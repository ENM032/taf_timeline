package com.timeline.api;

import com.timeline.config.Database;
import com.timeline.repository.FactRepository;
import com.timeline.repository.SqliteFactRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FactControllerTest {
    @Test
    void healthAndGetEmpty() {
        Database db = new Database("jdbc:sqlite:file:memdb2?mode=memory&cache=shared");
        db.init();
        FactRepository repo = new SqliteFactRepository(db);
        Javalin app = Javalin.create();
        new FactController(repo).register(app);
        app.get("/health", ctx -> ctx.result("ok"));
        JavalinTest.test(app, (server, client) -> {
            var res = client.get("/api/facts?year=2024&month=1");
            assertEquals(200, res.code());
            var health = client.get("/health");
            assertEquals(200, health.code());
        });
    }
}