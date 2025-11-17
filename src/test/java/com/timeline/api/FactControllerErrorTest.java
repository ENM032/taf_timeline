package com.timeline.api;

import com.timeline.config.Database;
import com.timeline.repository.FactRepository;
import com.timeline.repository.SqliteFactRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FactControllerErrorTest {
    private Javalin appWithHandlers() {
        Database db = new Database("jdbc:sqlite:file:errdb?mode=memory&cache=shared");
        db.init();
        FactRepository repo = new SqliteFactRepository(db);
        Javalin app = Javalin.create();
        GlobalExceptionHandler.register(app);
        new FactController(repo).register(app);
        return app;
    }

    @Test
    void missingQueryParamsReturns400() {
        JavalinTest.test(appWithHandlers(), (server, client) -> {
            var res = client.get("/api/facts");
            assertEquals(400, res.code());
        });
    }

    @Test
    void invalidMonthReturns400() {
        JavalinTest.test(appWithHandlers(), (server, client) -> {
            var res = client.get("/api/facts?year=2024&month=13");
            assertEquals(400, res.code());
        });
    }

    @Test
    void postValidationReturns422() {
        JavalinTest.test(appWithHandlers(), (server, client) -> {
            var res = client.post("/api/facts", "{\"title\":\"\"}");
            assertEquals(422, res.code());
        });
    }
}