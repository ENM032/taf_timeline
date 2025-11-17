package com.timeline.util;

public class EnvUtil {
    public static int getEnvInt(String name, int def) {
        try {
            String v = System.getenv(name);
            if (v == null || v.isBlank()) return def;
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    public static long getEnvLong(String name, long def) {
        try {
            String v = System.getenv(name);
            if (v == null || v.isBlank()) return def;
            return Long.parseLong(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    public static String getEnvString(String name, String def) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? def : v.trim();
    }
}