package com.timeline.util;

import java.net.URI;
import java.util.Arrays;

public class ValidationUtil {
    public static boolean validCategory(String c) {
        if (c == null || c.isBlank()) return true;
        return Arrays.asList(AppConstants.CATEGORY_ALLOW).contains(c);
    }

    public static boolean validUrl(String url) {
        if (url == null || url.isBlank()) return true;
        try {
            URI u = new URI(url);
            String s = u.getScheme();
            return s != null && (s.equals("http") || s.equals("https"));
        } catch (Exception e) {
            return false;
        }
    }

    public static String trimToLimit(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}