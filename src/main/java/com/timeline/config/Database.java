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
        config.setMaximumPoolSize(5);
        config.setPoolName("timeline-pool");
        dataSource = new HikariDataSource(config);
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
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
}