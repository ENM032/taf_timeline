package com.timeline.api;

import com.timeline.config.Database;
import com.timeline.model.Fact;
import com.timeline.repository.FactRepository;
import com.timeline.repository.SqliteFactRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FactControllerSearchTest {
    private Javalin appWithSeed(FactRepository repo) {
        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
        om.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        om.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Javalin app = Javalin.create(cfg -> cfg.jsonMapper(new io.javalin.json.JavalinJackson(om)));
        GlobalExceptionHandler.register(app);
        new FactController(repo).register(app);
        return app;
    }

    @Test
    void searchWithPagination() {
        Database db = new Database("jdbc:sqlite:file:searchdb?mode=memory&cache=shared");
        db.init();
        FactRepository repo = new SqliteFactRepository(db);
        for (int i = 1; i <= 5; i++) {
            Fact f = new Fact();
            f.setEventDate(LocalDate.of(2024, 1, i));
            f.setTitle("Item " + i);
            f.setSummary("Summary " + i);
            repo.add(f);
        }
        Javalin app = appWithSeed(repo);
        JavalinTest.test(app, (server, client) -> {
            var page1 = client.get("/api/facts/search?year=2024&month=1&page=0&size=2&sort=event_date,asc");
            assertEquals(200, page1.code(), page1.body().string());
            var page2 = client.get("/api/facts/search?year=2024&month=1&page=1&size=2&sort=event_date,asc");
            assertEquals(200, page2.code(), page2.body().string());
        });
    }
}