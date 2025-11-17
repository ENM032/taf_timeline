package com.timeline.repository;

import com.timeline.cache.LruCache;
import com.timeline.model.Fact;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class CachingFactRepository implements FactRepository {
    private final FactRepository delegate;
    private final LruCache<String, List<Fact>> monthCache = new LruCache<>(512, 5 * 60_000);
    private final LruCache<String, List<Fact>> dayCache = new LruCache<>(512, 5 * 60_000);

    public CachingFactRepository(FactRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Fact add(Fact fact) {
        Fact f = delegate.add(fact);
        invalidateFor(fact);
        return f;
    }

    @Override
    public Optional<Fact> getById(long id) { return delegate.getById(id); }

    @Override
    public List<Fact> getByMonth(YearMonth ym) {
        String key = "m:" + ym;
        List<Fact> cached = monthCache.get(key);
        if (cached != null) return cached;
        List<Fact> list = delegate.getByMonth(ym);
        monthCache.put(key, list);
        return list;
    }

    @Override
    public List<Fact> getByDate(LocalDate date) {
        String key = "d:" + date;
        List<Fact> cached = dayCache.get(key);
        if (cached != null) return cached;
        List<Fact> list = delegate.getByDate(date);
        dayCache.put(key, list);
        return list;
    }

    @Override
    public Optional<Fact> getRandom(YearMonth ym) { return delegate.getRandom(ym); }

    @Override
    public boolean existsByDateAndTitle(LocalDate date, String title) { return delegate.existsByDateAndTitle(date, title); }

    @Override
    public boolean update(long id, Fact fact) {
        boolean ok = delegate.update(id, fact);
        if (ok) invalidateFor(fact);
        return ok;
    }

    @Override
    public boolean delete(long id) {
        boolean ok = delegate.delete(id);
        if (ok) {
            monthCache.clear();
            dayCache.clear();
        }
        return ok;
    }

    @Override
    public List<Fact> search(Integer year, Integer month, String category, String q, int offset, int limit, String sortField, boolean asc) {
        return delegate.search(year, month, category, q, offset, limit, sortField, asc);
    }

    @Override
    public long count() { return delegate.count(); }

    private void invalidateFor(Fact fact) {
        if (fact.getEventDate() != null) {
            dayCache.invalidate("d:" + fact.getEventDate());
            YearMonth ym = YearMonth.of(fact.getEventDate().getYear(), fact.getEventDate().getMonth());
            monthCache.invalidate("m:" + ym);
        } else {
            monthCache.clear();
            dayCache.clear();
        }
    }
}