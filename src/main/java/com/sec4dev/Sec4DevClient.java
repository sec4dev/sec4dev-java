package com.sec4dev;

import com.sec4dev.models.RateLimitInfo;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Main client for the Sec4Dev Security Checks API.
 */
public final class Sec4DevClient {

    private static final String DEFAULT_BASE_URL = "https://api.sec4.dev/api/v1";
    private static final long DEFAULT_TIMEOUT_MS = 30_000;
    private static final int DEFAULT_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 1_000;

    private final com.sec4dev.HttpClient http;
    private final EmailService emailService;
    private final IPService ipService;
    private volatile RateLimitInfo rateLimit = new RateLimitInfo(0, 0, 0);

    private Sec4DevClient(Builder b) {
        String key = b.apiKey == null ? null : b.apiKey.trim();
        if (key == null || !key.startsWith("sec4_")) {
            throw new ValidationException("API key must start with sec4_", 422, null);
        }
        String baseUrl = b.baseUrl != null && !b.baseUrl.isEmpty() ? b.baseUrl.trim() : DEFAULT_BASE_URL;
        if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        HttpClient javaClient = null;
        if (b.timeoutMs > 0) {
            javaClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }
        Duration readTimeout = b.timeoutMs > 0 ? Duration.ofMillis(b.timeoutMs) : null;
        this.http = new com.sec4dev.HttpClient(
                baseUrl,
                key,
                javaClient,
                b.retries >= 0 ? b.retries : DEFAULT_RETRIES,
                b.retryDelayMs >= 0 ? b.retryDelayMs : DEFAULT_RETRY_DELAY_MS,
                readTimeout
        );
        com.sec4dev.HttpClient.RateLimitCallback cb = info -> {
            this.rateLimit = info;
            if (b.onRateLimit != null) b.onRateLimit.onRateLimit(info);
        };
        this.emailService = new EmailService(this.http, cb);
        this.ipService = new IPService(this.http, cb);
    }

    public EmailService getEmail() {
        return emailService;
    }

    public IPService getIp() {
        return ipService;
    }

    public RateLimitInfo getRateLimit() {
        return rateLimit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String apiKey;
        private String baseUrl;
        private long timeoutMs = DEFAULT_TIMEOUT_MS;
        private int retries = DEFAULT_RETRIES;
        private long retryDelayMs = DEFAULT_RETRY_DELAY_MS;
        private RateLimitCallback onRateLimit;

        private Builder() {}

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder timeout(long duration, TimeUnit unit) {
            this.timeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder retries(int retries) {
            this.retries = retries;
            return this;
        }

        public Builder retryDelay(long delayMs) {
            this.retryDelayMs = delayMs;
            return this;
        }

        public Builder onRateLimit(RateLimitCallback callback) {
            this.onRateLimit = callback;
            return this;
        }

        public Sec4DevClient build() {
            return new Sec4DevClient(this);
        }
    }

    @FunctionalInterface
    public interface RateLimitCallback {
        void onRateLimit(RateLimitInfo info);
    }
}
