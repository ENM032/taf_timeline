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
        String dbUrl = com.timeline.util.EnvUtil.getEnvString("DB_URL", "jdbc:sqlite:var/db/timeline.db");
        Database database = new Database(dbUrl);
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
        app.get("/health", ctx -> com.timeline.http.GzipJson.write(ctx, 200, new Status("ok")));
        app.get("/ready", ctx -> {
            try (java.sql.Connection c = database.getDataSource().getConnection(); java.sql.Statement s = c.createStatement()) {
                try (java.sql.ResultSet r = s.executeQuery("SELECT 1")) { com.timeline.http.GzipJson.write(ctx, 200, new Status("ready")); }
            } catch (Exception e) {
                com.timeline.http.GzipJson.write(ctx, 503, new Status("not-ready"));
            }
        });
        app.events(event -> event.serverStopped(() -> database.close()));
        int port = com.timeline.util.EnvUtil.getEnvInt("PORT", DEFAULT_PORT);
        app.start(port);
    }

    public record Status(String status) {}

    private static void seed(FactRepository repo) {}
}
