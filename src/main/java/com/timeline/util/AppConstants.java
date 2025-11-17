package com.timeline.util;

public class AppConstants {
    public static final int TITLE_MAX;
    public static final int SUMMARY_MAX;
    public static final String[] CATEGORY_ALLOW;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    static {
        TITLE_MAX = EnvUtil.getEnvInt("FACT_TITLE_MAX", 200);
        SUMMARY_MAX = EnvUtil.getEnvInt("FACT_SUMMARY_MAX", 2000);
        String cats = EnvUtil.getEnvString("FACT_CATEGORIES", "history,science,tech,culture,current");
        String[] parts = cats.split(",");
        java.util.List<String> list = new java.util.ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) list.add(t);
        }
        CATEGORY_ALLOW = list.toArray(new String[0]);
    }
}