package com.postwave.backend.auth;

import java.util.Map;

public record ApiError(
        String code,
        String message,
        Map<String, String> fieldErrors
) {
}