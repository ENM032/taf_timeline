package com.timeline.api;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.timeline.api.error.ErrorResponse;
import com.timeline.api.error.ValidationException;
import com.timeline.repository.DataAccessException;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.HandlerType;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

public class GlobalExceptionHandler {
    public static final String REQ_ID_ATTR = "requestId";

    public static void register(Javalin app) {
        app.before(ctx -> {
            String reqId = ctx.header("X-Request-Id");
            if (reqId == null || reqId.isBlank()) reqId = UUID.randomUUID().toString();
            ctx.attribute(REQ_ID_ATTR, reqId);
            ctx.contentType("application/json");
            com.timeline.rate.RateLimiter rl = Holder.RL;
            String ip = ctx.req().getRemoteAddr();
            if (!rl.allow(ip)) throw new io.javalin.http.HttpResponseException(429, "Too Many Requests");
            if (ctx.method() == HandlerType.POST || ctx.method() == HandlerType.PUT) {
                String ct = ctx.header("Content-Type");
                if (ct == null || !ct.toLowerCase().contains("application/json")) throw new io.javalin.http.UnsupportedMediaTypeResponse("Content-Type must be application/json");
                long cl = ctx.req().getContentLengthLong();
                long max = com.timeline.util.EnvUtil.getEnvLong("REQUEST_MAX_BYTES", 2_000_000);
                if (cl > 0 && cl > max) throw new io.javalin.http.HttpResponseException(413, "Payload Too Large");
            }
        });
        app.after(ctx -> {
            String reqId = ctx.attribute(REQ_ID_ATTR);
            if (reqId != null) ctx.header("X-Request-Id", reqId);
        });

        app.exception(ValidationException.class, (e, ctx) -> {
            write(ctx, 422, "Unprocessable Entity", e.getMessage() == null ? "Validation failed" : e.getMessage(), e.getFieldErrors());
        });
        app.exception(BadRequestResponse.class, (e, ctx) -> {
            write(ctx, 400, "Bad Request", e.getMessage(), null);
        });
        app.exception(NotFoundResponse.class, (e, ctx) -> {
            write(ctx, 404, "Not Found", e.getMessage(), null);
        });
        app.exception(JsonParseException.class, (e, ctx) -> {
            write(ctx, 400, "Bad Request", "Malformed JSON", null);
        });
        app.exception(JsonMappingException.class, (e, ctx) -> {
            write(ctx, 400, "Bad Request", "Invalid JSON structure", null);
        });
        app.exception(DataAccessException.class, (e, ctx) -> {
            Throwable c = e.getCause();
            if (c instanceof java.sql.SQLException && (c.getMessage() != null && c.getMessage().toLowerCase().contains("fts"))) {
                write(ctx, 400, "Bad Request", "Invalid search query", null, "invalid_search_query", null);
            } else {
                write(ctx, 503, "Service Unavailable", "Database unavailable", null, "db_unavailable", null);
            }
        });
        app.exception(Exception.class, (e, ctx) -> {
            java.util.Map<String,String> det = new java.util.LinkedHashMap<>();
            det.put("exception", e.getClass().getName());
            det.put("message", e.getMessage());
            write(ctx, 500, "Internal Server Error", "Unexpected error", det, "unexpected_error", null);
        });
        app.error(404, ctx -> write(ctx, 404, "Not Found", "Route not found", null, "not_found", null));
        app.error(405, ctx -> write(ctx, 405, "Method Not Allowed", "Method not allowed", null, "method_not_allowed", null));
        app.error(415, ctx -> write(ctx, 415, "Unsupported Media Type", "Content-Type must be application/json", null, "unsupported_media_type", null));
    }

    private static void write(Context ctx, int status, String error, String message, Map<String, String> details) {
        write(ctx, status, error, message, details, null, null);
    }

    private static void write(Context ctx, int status, String error, String message, Map<String, String> details, String code, String type) {
        String reqId = ctx.attribute(REQ_ID_ATTR);
        ErrorResponse body = new ErrorResponse(OffsetDateTime.now(ZoneOffset.UTC), status, error, message, ctx.path(), reqId, code, type, details);
        com.timeline.http.GzipJson.write(ctx, status, body);
    }

    private static class Holder {
        static final com.timeline.rate.RateLimiter RL = new com.timeline.rate.RateLimiter(
                com.timeline.util.EnvUtil.getEnvInt("RATE_LIMIT_PER_MIN", 120),
                com.timeline.util.EnvUtil.getEnvLong("RATE_LIMIT_WINDOW_MS", 60_000));
        }
}