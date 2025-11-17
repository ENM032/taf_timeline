package com.timeline.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.http.Context;

import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

public class GzipJson {
    private static final ObjectMapper OM;
    static {
        OM = new ObjectMapper();
        OM.registerModule(new JavaTimeModule());
        OM.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void write(Context ctx, int status, Object body) {
        try {
            byte[] data = OM.writeValueAsBytes(body);
            String ae = ctx.header("Accept-Encoding");
            boolean gzip = ae != null && ae.toLowerCase(Locale.ROOT).contains("gzip");
            int min = com.timeline.util.EnvUtil.getEnvInt("GZIP_MIN_BYTES", 1024);
            if (!gzip || data.length < min) {
                ctx.status(status).json(body);
                return;
            }
            ctx.header("Content-Encoding", "gzip");
            ctx.header("Vary", "Accept-Encoding");
            ctx.contentType("application/json");
            ctx.status(status);
            OutputStream os = ctx.res().getOutputStream();
            try (GZIPOutputStream gos = new GZIPOutputStream(os)) {
                gos.write(data);
            }
        } catch (Exception e) {
            ctx.status(status).json(body);
        }
    }
}