package com.timeline;

import com.timeline.api.FactController;
import com.timeline.api.GlobalExceptionHandler;
import com.timeline.config.Database;
import com.timeline.bootstrap.Seeder;
import com.timeline.model.Fact;
import com.timeline.repository.FactRepository;
import com.timeline.repository.SqliteFactRepository;
import com.timeline.repository.CachingFactRepository;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;

public class App {
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        Database database = new Database("jdbc:sqlite:var/db/timeline.db");
        database.init();
        FactRepository repo = new CachingFactRepository(new SqliteFactRepository(database));
        Seeder.seedIfEmpty(repo);
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.jsonMapper(new JavalinJackson(om));
        });
        GlobalExceptionHandler.register(app);
        new FactController(repo).register(app);
        app.get("/health", ctx -> ctx.json(new Status("ok")));
        app.get("/ready", ctx -> {
            try (java.sql.Connection c = database.getDataSource().getConnection(); java.sql.Statement s = c.createStatement()) {
                try (java.sql.ResultSet r = s.executeQuery("SELECT 1")) { ctx.json(new Status("ready")); }
            } catch (Exception e) {
                ctx.status(503).json(new Status("not-ready"));
            }
        });
        app.events(event -> event.serverStopped(() -> database.close()));
        app.start(DEFAULT_PORT);
    }

    public record Status(String status) {}

    private static void seed(FactRepository repo) {}
}
