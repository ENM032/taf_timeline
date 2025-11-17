package com.timeline.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private final String jdbcUrl;
    private HikariDataSource dataSource;

    public Database(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void init() {
        ensureParentDirectory();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setMinimumIdle(getEnvInt("DB_POOL_MIN", 1));
        config.setMaximumPoolSize(getEnvInt("DB_POOL_MAX", 6));
        config.setConnectionTimeout(getEnvLong("DB_CONN_TIMEOUT_MS", 5000));
        config.setIdleTimeout(getEnvLong("DB_IDLE_TIMEOUT_MS", 300000));
        config.setMaxLifetime(getEnvLong("DB_MAX_LIFETIME_MS", 1800000));
        config.setValidationTimeout(getEnvLong("DB_VALIDATION_TIMEOUT_MS", 3000));
        long leak = getEnvLong("DB_LEAK_DETECTION_MS", 20000);
        if (leak > 0) config.setLeakDetectionThreshold(leak);
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("timeline-pool");
        dataSource = new HikariDataSource(config);
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            boolean isMemory = jdbcUrl.contains("mode=memory") || jdbcUrl.contains(":memory:");
            st.execute("PRAGMA busy_timeout=5000");
            st.execute("PRAGMA foreign_keys=ON");
            if (!isMemory) {
                try { st.execute("PRAGMA journal_mode=WAL"); } catch (SQLException ignored) {}
                try { st.execute("PRAGMA synchronous=NORMAL"); } catch (SQLException ignored) {}
                try { st.execute("PRAGMA cache_size=-20000"); } catch (SQLException ignored) {}
            }
            st.executeUpdate("CREATE TABLE IF NOT EXISTS facts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "event_date TEXT NOT NULL," +
                    "title TEXT NOT NULL," +
                    "summary TEXT NOT NULL," +
                    "category TEXT," +
                    "source_url TEXT," +
                    "created_at TEXT NOT NULL)"
            );
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_facts_event_date ON facts(event_date)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_facts_category ON facts(category)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_facts_created_at ON facts(created_at)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_facts_event_date_title ON facts(event_date, title)");

            st.executeUpdate("CREATE VIRTUAL TABLE IF NOT EXISTS facts_fts USING fts5(title, summary, content='facts', content_rowid='id')");
            st.executeUpdate("CREATE TRIGGER IF NOT EXISTS facts_ai AFTER INSERT ON facts BEGIN INSERT INTO facts_fts(rowid, title, summary) VALUES (new.id, new.title, new.summary); END");
            st.executeUpdate("CREATE TRIGGER IF NOT EXISTS facts_au AFTER UPDATE ON facts BEGIN UPDATE facts_fts SET title=new.title, summary=new.summary WHERE rowid=new.id; END");
            st.executeUpdate("CREATE TRIGGER IF NOT EXISTS facts_ad AFTER DELETE ON facts BEGIN DELETE FROM facts_fts WHERE rowid=old.id; END");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private void ensureParentDirectory() {
        String prefix = "jdbc:sqlite:";
        if (jdbcUrl.startsWith(prefix)) {
            String path = jdbcUrl.substring(prefix.length());
            java.io.File file = new java.io.File(path);
            java.io.File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
        }
    }

    public void close() {
        if (dataSource != null) dataSource.close();
    }

    private int getEnvInt(String name, int def) {
        try {
            String v = System.getenv(name);
            if (v == null || v.isBlank()) return def;
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private long getEnvLong(String name, long def) {
        try {
            String v = System.getenv(name);
            if (v == null || v.isBlank()) return def;
            return Long.parseLong(v.trim());
        } catch (Exception e) {
            return def;
        }
    }
}