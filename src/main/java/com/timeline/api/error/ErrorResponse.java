package com.timeline.api.error;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(OffsetDateTime timestamp,
                            int status,
                            String error,
                            String message,
                            String path,
                            String requestId,
                            Map<String, String> details) {}