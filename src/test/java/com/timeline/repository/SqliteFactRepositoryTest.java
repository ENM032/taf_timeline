package com.timeline.repository;

import com.timeline.config.Database;
import com.timeline.model.Fact;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteFactRepositoryTest {
    @Test
    void addAndQueryByMonth() {
        Database db = new Database("jdbc:sqlite:file:memdb1?mode=memory&cache=shared");
        db.init();
        FactRepository repo = new SqliteFactRepository(db);
        Fact f = new Fact();
        f.setEventDate(LocalDate.of(2024, 1, 5));
        f.setTitle("Test");
        f.setSummary("Summary");
        repo.add(f);
        List<Fact> jan = repo.getByMonth(YearMonth.of(2024, 1));
        assertEquals(1, jan.size());
        assertEquals("Test", jan.get(0).getTitle());
        List<Fact> feb = repo.getByMonth(YearMonth.of(2024, 2));
        assertEquals(0, feb.size());
    }
}