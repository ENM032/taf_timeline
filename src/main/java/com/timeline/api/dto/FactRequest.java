package com.timeline.api.dto;

public record FactRequest(String eventDate, String title, String summary, String category, String sourceUrl) {}