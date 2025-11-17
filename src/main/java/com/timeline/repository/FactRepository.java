package com.timeline.repository;

import com.timeline.model.Fact;

import java.time.YearMonth;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FactRepository {
    Fact add(Fact fact);
    Optional<Fact> getById(long id);
    List<Fact> getByMonth(YearMonth ym);
    List<Fact> getByDate(LocalDate date);
    Optional<Fact> getRandom(YearMonth ym);
    boolean existsByDateAndTitle(LocalDate date, String title);
    boolean update(long id, Fact fact);
    boolean delete(long id);
    List<Fact> search(Integer year, Integer month, String category, String q, int offset, int limit, String sortField, boolean asc);
    long count();
}