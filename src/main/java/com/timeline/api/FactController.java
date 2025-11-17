package com.timeline.api;

import com.timeline.api.dto.FactRequest;
import com.timeline.api.error.ValidationException;
import com.timeline.model.Fact;
import com.timeline.repository.FactRepository;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class FactController {
    private final FactRepository repo;

    public FactController(FactRepository repo) {
        this.repo = repo;
    }

    public void register(Javalin app) {
        app.get("/api/facts", ctx -> {
            String yearStr = ctx.queryParam("year");
            String monthStr = ctx.queryParam("month");
            if (yearStr == null || monthStr == null) throw new BadRequestResponse("year and month are required");
            int year = parseInt(yearStr, "year");
            int month = parseInt(monthStr, "month");
            if (month < 1 || month > 12) throw new BadRequestResponse("month must be 1-12");
            YearMonth ym = YearMonth.of(year, month);
            List<Fact> list = repo.getByMonth(ym);
            ctx.json(list);
        });

        app.get("/api/facts/on", ctx -> {
            String dateStr = ctx.queryParam("date");
            if (dateStr == null || dateStr.isBlank()) throw new BadRequestResponse("date is required");
            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                throw new BadRequestResponse("date must be YYYY-MM-DD");
            }
            ctx.json(repo.getByDate(date));
        });

        app.get("/api/facts/random", ctx -> {
            String yearStr = ctx.queryParam("year");
            String monthStr = ctx.queryParam("month");
            if (yearStr == null || monthStr == null) throw new BadRequestResponse("year and month are required");
            int year = parseInt(yearStr, "year");
            int month = parseInt(monthStr, "month");
            if (month < 1 || month > 12) throw new BadRequestResponse("month must be 1-12");
            YearMonth ym = YearMonth.of(year, month);
            Fact f = repo.getRandom(ym).orElseThrow(() -> new NotFoundResponse("not found"));
            ctx.json(f);
        });

        app.get("/api/facts/search", ctx -> {
            Integer year = ctx.queryParam("year") != null ? parseInt(ctx.queryParam("year"), "year") : null;
            Integer month = ctx.queryParam("month") != null ? parseInt(ctx.queryParam("month"), "month") : null;
            if (month != null && (month < 1 || month > 12)) throw new BadRequestResponse("month must be 1-12");
            String category = ctx.queryParam("category");
            String q = ctx.queryParam("q");
            int page = ctx.queryParam("page") != null ? Math.max(0, parseInt(ctx.queryParam("page"), "page")) : 0;
            int size = ctx.queryParam("size") != null ? Math.min(100, Math.max(1, parseInt(ctx.queryParam("size"), "size"))) : 20;
            String sort = ctx.queryParam("sort");
            String sortField = "event_date";
            boolean asc = true;
            if (sort != null && !sort.isBlank()) {
                String[] parts = sort.split(",");
                sortField = parts[0];
                if (parts.length > 1) asc = !parts[1].equalsIgnoreCase("desc");
            }
            int offset = page * size;
            ctx.json(repo.search(year, month, category, q, offset, size, sortField, asc));
        });

        app.get("/api/facts/{id}", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            Fact f = repo.getById(id).orElseThrow(() -> new NotFoundResponse("not found"));
            ctx.json(f);
        });

        app.post("/api/facts", ctx -> {
            FactRequest req = ctx.bodyAsClass(FactRequest.class);
            java.util.Map<String,String> errors = new java.util.LinkedHashMap<>();
            if (req.title() == null || req.title().isBlank()) errors.put("title", "required");
            if (req.summary() == null || req.summary().isBlank()) errors.put("summary", "required");
            if (req.eventDate() == null || req.eventDate().isBlank()) errors.put("eventDate", "required");
            LocalDate date = null;
            if (!errors.containsKey("eventDate")) {
                try {
                    date = LocalDate.parse(req.eventDate());
                } catch (Exception e) {
                    errors.put("eventDate", "must be YYYY-MM-DD");
                }
            }
            if (!errors.isEmpty()) throw new ValidationException("Validation failed", errors);
            Fact f = new Fact();
            f.setEventDate(date);
            f.setTitle(req.title());
            f.setSummary(req.summary());
            f.setCategory(req.category());
            f.setSourceUrl(req.sourceUrl());
            Fact saved = repo.add(f);
            ctx.status(201).json(saved);
        });

        app.put("/api/facts/{id}", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            if (repo.getById(id).isEmpty()) throw new NotFoundResponse("not found");
            FactRequest req = ctx.bodyAsClass(FactRequest.class);
            java.util.Map<String,String> errors = new java.util.LinkedHashMap<>();
            if (req.title() == null || req.title().isBlank()) errors.put("title", "required");
            if (req.summary() == null || req.summary().isBlank()) errors.put("summary", "required");
            if (req.eventDate() == null || req.eventDate().isBlank()) errors.put("eventDate", "required");
            LocalDate date = null;
            if (!errors.containsKey("eventDate")) {
                try {
                    date = LocalDate.parse(req.eventDate());
                } catch (Exception e) {
                    errors.put("eventDate", "must be YYYY-MM-DD");
                }
            }
            if (!errors.isEmpty()) throw new ValidationException("Validation failed", errors);
            Fact f = new Fact();
            f.setEventDate(date);
            f.setTitle(req.title());
            f.setSummary(req.summary());
            f.setCategory(req.category());
            f.setSourceUrl(req.sourceUrl());
            boolean ok = repo.update(id, f);
            if (!ok) throw new NotFoundResponse("not found");
            ctx.json(repo.getById(id).orElseThrow(() -> new NotFoundResponse("not found")));
        });

        app.delete("/api/facts/{id}", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            boolean ok = repo.delete(id);
            if (!ok) throw new NotFoundResponse("not found");
            ctx.status(204);
        });
    }

    private int parseInt(String value, String name) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadRequestResponse(name + " must be an integer");
        }
    }
}