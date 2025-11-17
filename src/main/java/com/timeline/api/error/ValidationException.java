package com.timeline.api.error;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors == null ? Collections.emptyMap() : new LinkedHashMap<>(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }
}