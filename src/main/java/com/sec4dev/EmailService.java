package com.sec4dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sec4dev.models.EmailCheckResult;

import java.util.Map;

/** Email check service. */
public final class EmailService {

    private final com.sec4dev.HttpClient http;
    private final com.sec4dev.HttpClient.RateLimitCallback onRateLimit;
    private final ObjectMapper mapper = new ObjectMapper();

    EmailService(com.sec4dev.HttpClient http, com.sec4dev.HttpClient.RateLimitCallback onRateLimit) {
        this.http = http;
        this.onRateLimit = onRateLimit;
    }

    /** Check if an email uses a disposable domain. */
    public EmailCheckResult check(String email) {
        Validation.validateEmail(email);
        String path = "/email/check";
        Map<String, String> body = Map.of("email", email.trim());
        byte[] out = http.post(path, body, onRateLimit);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = mapper.readValue(out, Map.class);
            String e = (String) m.getOrDefault("email", email);
            String domain = (String) m.getOrDefault("domain", "");
            boolean disposable = Boolean.TRUE.equals(m.get("is_disposable"));
            return new EmailCheckResult(e, domain, disposable);
        } catch (Exception e) {
            throw new Sec4DevException("Failed to parse response: " + e.getMessage(), 0, null);
        }
    }

    /** Return true if the email domain is disposable. */
    public boolean isDisposable(String email) {
        return check(email).isDisposable();
    }
}
