package com.timeline.api;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.timeline.api.error.ErrorResponse;
import com.timeline.api.error.ValidationException;
import com.timeline.repository.DataAccessException;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
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
            write(ctx, 503, "Service Unavailable", "Database unavailable", null);
        });
        app.exception(Exception.class, (e, ctx) -> {
            java.util.Map<String,String> det = new java.util.LinkedHashMap<>();
            det.put("exception", e.getClass().getName());
            det.put("message", e.getMessage());
            write(ctx, 500, "Internal Server Error", "Unexpected error", det);
        });
        app.error(404, ctx -> write(ctx, 404, "Not Found", "Route not found", null));
    }

    private static void write(Context ctx, int status, String error, String message, Map<String, String> details) {
        String reqId = ctx.attribute(REQ_ID_ATTR);
        ErrorResponse body = new ErrorResponse(OffsetDateTime.now(ZoneOffset.UTC), status, error, message, ctx.path(), reqId, details);
        ctx.status(status).json(body);
    }
}