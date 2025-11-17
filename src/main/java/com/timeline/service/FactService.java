package com.timeline.service;

import com.timeline.model.Fact;
import com.timeline.repository.FactRepository;
import com.timeline.util.AppConstants;
import com.timeline.util.ValidationUtil;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FactService {
    private final FactRepository repo;

    public FactService(FactRepository repo) { this.repo = repo; }

    public Map<String,String> validate(String eventDate, String title, String summary, String category, String sourceUrl) {
        Map<String,String> errors = new LinkedHashMap<>();
        if (title == null || title.isBlank()) errors.put("title", "required");
        if (summary == null || summary.isBlank()) errors.put("summary", "required");
        if (eventDate == null || eventDate.isBlank()) errors.put("eventDate", "required");
        LocalDate date = null;
        if (!errors.containsKey("eventDate")) {
            try { date = LocalDate.parse(eventDate); } catch (Exception e) { errors.put("eventDate", "must be YYYY-MM-DD"); }
        }
        if (title != null && title.length() > AppConstants.TITLE_MAX) errors.put("title", "max " + AppConstants.TITLE_MAX);
        if (summary != null && summary.length() > AppConstants.SUMMARY_MAX) errors.put("summary", "max " + AppConstants.SUMMARY_MAX);
        if (!ValidationUtil.validCategory(category)) errors.put("category", "invalid");
        if (!ValidationUtil.validUrl(sourceUrl)) errors.put("sourceUrl", "invalid");
        return errors;
    }

    public Fact create(LocalDate date, String title, String summary, String category, String sourceUrl) {
        Fact f = new Fact();
        f.setEventDate(date);
        f.setTitle(ValidationUtil.trimToLimit(title, AppConstants.TITLE_MAX));
        f.setSummary(ValidationUtil.trimToLimit(summary, AppConstants.SUMMARY_MAX));
        f.setCategory(category);
        f.setSourceUrl(sourceUrl);
        return repo.add(f);
    }

    public boolean update(long id, LocalDate date, String title, String summary, String category, String sourceUrl) {
        Fact f = new Fact();
        f.setEventDate(date);
        f.setTitle(ValidationUtil.trimToLimit(title, AppConstants.TITLE_MAX));
        f.setSummary(ValidationUtil.trimToLimit(summary, AppConstants.SUMMARY_MAX));
        f.setCategory(category);
        f.setSourceUrl(sourceUrl);
        return repo.update(id, f);
    }

    public List<Fact> month(YearMonth ym) { return repo.getByMonth(ym); }
    public List<Fact> day(LocalDate d) { return repo.getByDate(d); }
    public List<Fact> search(Integer year, Integer month, String category, String q, int offset, int size, String sortField, boolean asc) {
        return repo.search(year, month, category, q, offset, size, sortField, asc);
    }
}