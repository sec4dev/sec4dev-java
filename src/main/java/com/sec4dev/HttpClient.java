package com.sec4dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sec4dev.models.RateLimitInfo;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Internal HTTP client with retry and rate limit handling.
 */
final class HttpClient {

    private static final String SDK_VERSION = "1.0.0";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    private final String baseUrl;
    private final String apiKey;
    private final java.net.http.HttpClient client;
    private final int retries;
    private final long retryDelayMs;
    private final Duration readTimeout;
    private final ObjectMapper mapper = new ObjectMapper();

    HttpClient(String baseUrl, String apiKey, java.net.http.HttpClient client,
               int retries, long retryDelayMs, Duration readTimeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.client = client != null ? client : defaultClient();
        this.retries = retries;
        this.retryDelayMs = retryDelayMs;
        this.readTimeout = readTimeout != null && !readTimeout.isZero() ? readTimeout : DEFAULT_READ_TIMEOUT;
    }

    private static java.net.http.HttpClient defaultClient() {
        return java.net.http.HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    private static int getIntHeader(HttpResponse<?> resp, String name, int def) {
        String v = resp.headers().firstValue(name).orElse(null);
        if (v == null) return def;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    RateLimitInfo parseRateLimit(HttpResponse<?> resp) {
        return new RateLimitInfo(
                getIntHeader(resp, "X-RateLimit-Limit", 0),
                getIntHeader(resp, "X-RateLimit-Remaining", 0),
                getIntHeader(resp, "X-RateLimit-Reset", 0)
        );
    }

    private String messageFromBody(byte[] body) {
        if (body == null || body.length == 0) return "Unknown error";
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = mapper.readValue(body, Map.class);
            Object d = m.get("detail");
            return d != null ? d.toString() : "Unknown error";
        } catch (Exception e) {
            return "Unknown error";
        }
    }

    private Sec4DevException exceptionFrom(int statusCode, byte[] body, HttpResponse<?> resp) {
        String message = messageFromBody(body);
        int retryAfter = 0;
        int limit = 0;
        int remaining = 0;
        if (resp != null) {
            limit = getIntHeader(resp, "X-RateLimit-Limit", 0);
            remaining = getIntHeader(resp, "X-RateLimit-Remaining", 0);
            String ra = resp.headers().firstValue("Retry-After").orElse(null);
            if (ra != null) {
                try {
                    retryAfter = Integer.parseInt(ra);
                } catch (NumberFormatException ignored) {}
            }
        }
        Object bodyObj = null;
        try {
            if (body != null && body.length > 0) bodyObj = mapper.readValue(body, Map.class);
        } catch (Exception ignored) {}
        switch (statusCode) {
            case 401: return new AuthenticationException(message, statusCode, bodyObj);
            case 402: return new PaymentRequiredException(message, statusCode, bodyObj);
            case 403: return new ForbiddenException(message, statusCode, bodyObj);
            case 404: return new NotFoundException(message, statusCode, bodyObj);
            case 422: return new ValidationException(message, statusCode, bodyObj);
            case 429: return new RateLimitException(message, statusCode, bodyObj, retryAfter, limit, remaining);
            default:
                if (statusCode >= 500) return new ServerException(message, statusCode, bodyObj);
                return new Sec4DevException(message, statusCode, bodyObj);
        }
    }

    private static boolean isRetryable(int statusCode, boolean networkError) {
        if (networkError) return true;
        return statusCode == 429 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    byte[] post(String path, Object body, RateLimitCallback onRateLimit) throws Sec4DevException {
        String uri = baseUrl + path;
        String bodyJson;
        try {
            bodyJson = body != null ? mapper.writeValueAsString(body) : "{}";
        } catch (Exception e) {
            throw new Sec4DevException("Failed to serialize request", 0, null);
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("X-API-Key", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "sec4dev-java/" + SDK_VERSION)
                .timeout(readTimeout)
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson));

        Sec4DevException lastException = null;
        int lastStatus = 0;
        byte[] lastBody = null;
        HttpResponse<byte[]> lastResp = null;

        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                HttpRequest req = builder.build();
                HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
                byte[] respBody = resp.body();

                RateLimitInfo rl = parseRateLimit(resp);
                if (onRateLimit != null) onRateLimit.onRateLimit(rl);

                if (resp.statusCode() == 429) {
                    int retryAfter = getIntHeader(resp, "Retry-After", 60);
                    if (attempt < retries) {
                        try {
                            Thread.sleep(retryAfter * 1000L);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new Sec4DevException("Interrupted", 0, null);
                        }
                        continue;
                    }
                    throw exceptionFrom(429, respBody, resp);
                }

                if (resp.statusCode() >= 400) {
                    Sec4DevException ex = exceptionFrom(resp.statusCode(), respBody, resp);
                    if (!isRetryable(resp.statusCode(), false)) throw ex;
                    lastException = ex;
                    lastStatus = resp.statusCode();
                    lastBody = respBody;
                    lastResp = resp;
                    if (attempt < retries) {
                        long delay = retryDelayMs * (1L << attempt) + ThreadLocalRandom.current().nextInt(0, 101);
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new Sec4DevException("Interrupted", 0, null);
                        }
                        continue;
                    }
                    throw ex;
                }

                return respBody;
            } catch (Sec4DevException e) {
                throw e;
            } catch (Exception e) {
                lastException = new Sec4DevException(e.getMessage(), 0, null);
                if (attempt < retries) {
                    long delay = retryDelayMs * (1L << attempt) + ThreadLocalRandom.current().nextInt(0, 101);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Sec4DevException("Interrupted", 0, null);
                    }
                    continue;
                }
                if (e instanceof RuntimeException) throw (RuntimeException) e;
                throw new Sec4DevException(e.getMessage(), 0, null);
            }
        }

        if (lastException != null && lastResp != null) {
            throw exceptionFrom(lastStatus, lastBody, lastResp);
        }
        throw lastException != null ? lastException : new Sec4DevException("Request failed after retries", 0, null);
    }

    interface RateLimitCallback {
        void onRateLimit(RateLimitInfo info);
    }
}
