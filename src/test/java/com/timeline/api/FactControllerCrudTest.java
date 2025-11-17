package com.timeline.api;

import com.timeline.config.Database;
import com.timeline.repository.FactRepository;
import com.timeline.repository.SqliteFactRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FactControllerCrudTest {
    private Javalin appWithRepo(FactRepository repo) {
        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
        om.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        om.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Javalin app = Javalin.create(cfg -> cfg.jsonMapper(new io.javalin.json.JavalinJackson(om)));
        GlobalExceptionHandler.register(app);
        new FactController(repo).register(app);
        return app;
    }

    @Test
    void createUpdateDeleteFlow() {
        Database db = new Database("jdbc:sqlite:file:cruddb?mode=memory&cache=shared");
        db.init();
        FactRepository repo = new SqliteFactRepository(db);
        Javalin app = appWithRepo(repo);

        JavalinTest.test(app, (server, client) -> {
            var createRes = client.post("/api/facts", "{\"eventDate\":\"2024-01-02\",\"title\":\"T\",\"summary\":\"S\"}");
            assertEquals(201, createRes.code());
            var get1 = client.get("/api/facts/1");
            assertEquals(200, get1.code());

            var upd = client.put("/api/facts/1", "{\"eventDate\":\"2024-01-03\",\"title\":\"T2\",\"summary\":\"S2\"}");
            assertTrue(upd.code() == 200 || upd.code() == 201);
            var del = client.delete("/api/facts/1");
            assertEquals(204, del.code());
            var nf = client.get("/api/facts/1");
            assertEquals(404, nf.code());
        });
    }
}