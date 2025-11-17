package com.timeline.repository;

import com.timeline.config.Database;
import com.timeline.model.Fact;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteFactRepository implements FactRepository {
    private final DataSource dataSource;

    public SqliteFactRepository(Database database) {
        this.dataSource = database.getDataSource();
    }

    @Override
    public Fact add(Fact fact) {
        String sql = "INSERT INTO facts(event_date, title, summary, category, source_url, created_at) VALUES(?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fact.getEventDate().toString());
            ps.setString(2, fact.getTitle());
            ps.setString(3, fact.getSummary());
            ps.setString(4, fact.getCategory());
            ps.setString(5, fact.getSourceUrl());
            OffsetDateTime created = fact.getCreatedAt() != null ? fact.getCreatedAt() : OffsetDateTime.now(ZoneOffset.UTC);
            ps.setString(6, created.toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    fact.setId(rs.getLong(1));
                    fact.setCreatedAt(created);
                }
            }
            return fact;
        } catch (SQLException e) {
            throw new DataAccessException("insert failed", e);
        }
    }

    @Override
    public Optional<Fact> getById(long id) {
        String sql = "SELECT id, event_date, title, summary, category, source_url, created_at FROM facts WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("query by id failed", e);
        }
    }

    @Override
    public List<Fact> getByMonth(YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        String sql = "SELECT id, event_date, title, summary, category, source_url, created_at FROM facts WHERE event_date BETWEEN ? AND ? ORDER BY event_date";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start.toString());
            ps.setString(2, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<Fact> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("query by month failed", e);
        }
    }

    @Override
    public List<Fact> getByDate(LocalDate date) {
        String sql = "SELECT id, event_date, title, summary, category, source_url, created_at FROM facts WHERE event_date = ? ORDER BY id";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<Fact> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("query by date failed", e);
        }
    }

    @Override
    public Optional<Fact> getRandom(YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement cnt = conn.prepareStatement("SELECT COUNT(1) FROM facts WHERE event_date BETWEEN ? AND ?")) {
                cnt.setString(1, start.toString());
                cnt.setString(2, end.toString());
                int count = 0;
                try (ResultSet rs = cnt.executeQuery()) { if (rs.next()) count = rs.getInt(1); }
                if (count == 0) return Optional.empty();
                int offset = Math.abs(new java.util.Random().nextInt()) % count;
                try (PreparedStatement ps = conn.prepareStatement("SELECT id, event_date, title, summary, category, source_url, created_at FROM facts WHERE event_date BETWEEN ? AND ? LIMIT 1 OFFSET ?")) {
                    ps.setString(1, start.toString());
                    ps.setString(2, end.toString());
                    ps.setInt(3, offset);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) return Optional.of(map(rs));
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("random failed", e);
        }
    }

    @Override
    public boolean existsByDateAndTitle(LocalDate date, String title) {
        String sql = "SELECT 1 FROM facts WHERE event_date=? AND title=? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            ps.setString(2, title);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new DataAccessException("exists by date/title failed", e);
        }
    }

    @Override
    public boolean update(long id, Fact fact) {
        String sql = "UPDATE facts SET event_date=?, title=?, summary=?, category=?, source_url=? WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fact.getEventDate().toString());
            ps.setString(2, fact.getTitle());
            ps.setString(3, fact.getSummary());
            ps.setString(4, fact.getCategory());
            ps.setString(5, fact.getSourceUrl());
            ps.setLong(6, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("update failed", e);
        }
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM facts WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("delete failed", e);
        }
    }

    @Override
    public List<Fact> search(Integer year, Integer month, String category, String q, int offset, int limit, String sortField, boolean asc) {
        boolean useFts = q != null && !q.isBlank();
        StringBuilder sb = new StringBuilder();
        if (useFts) {
            sb.append("SELECT f.id, f.event_date, f.title, f.summary, f.category, f.source_url, f.created_at FROM facts f JOIN facts_fts fts ON fts.rowid = f.id WHERE fts MATCH ?");
        } else {
            sb.append("SELECT id, event_date, title, summary, category, source_url, created_at FROM facts WHERE 1=1");
        }
        List<Object> params = new ArrayList<>();
        if (useFts) {
            params.add(q);
        }
        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            sb.append(useFts ? " AND f.event_date BETWEEN ? AND ?" : " AND event_date BETWEEN ? AND ?");
            params.add(ym.atDay(1).toString());
            params.add(ym.atEndOfMonth().toString());
        } else if (year != null) {
            String y = String.format("%04d", year);
            sb.append(useFts ? " AND f.event_date BETWEEN ? AND ?" : " AND event_date BETWEEN ? AND ?");
            params.add(y + "-01-01");
            params.add(y + "-12-31");
        }
        if (category != null && !category.isBlank()) {
            sb.append(useFts ? " AND f.category = ?" : " AND category = ?");
            params.add(category);
        }
        String sf = (sortField != null && ("event_date".equalsIgnoreCase(sortField) || "created_at".equalsIgnoreCase(sortField) || "title".equalsIgnoreCase(sortField))) ? sortField : "event_date";
        sb.append(" ORDER BY ").append(useFts ? ("f." + sf) : sf).append(asc ? " ASC" : " DESC");
        sb.append(" LIMIT ? OFFSET ?");
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            for (Object p : params) {
                ps.setObject(idx++, p);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Fact> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("search failed", e);
        }
    }
    @Override
    public long count() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(1) FROM facts")) {
            if (rs.next()) return rs.getLong(1);
            return 0;
        } catch (SQLException e) {
            throw new DataAccessException("count failed", e);
        }
    }

    private Fact map(ResultSet rs) throws SQLException {
        Fact f = new Fact();
        f.setId(rs.getLong("id"));
        f.setEventDate(LocalDate.parse(rs.getString("event_date")));
        f.setTitle(rs.getString("title"));
        f.setSummary(rs.getString("summary"));
        f.setCategory(rs.getString("category"));
        f.setSourceUrl(rs.getString("source_url"));
        f.setCreatedAt(OffsetDateTime.parse(rs.getString("created_at")));
        return f;
    }
}