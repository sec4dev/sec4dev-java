package com.sec4dev;

/** 429 - Rate limit exceeded. */
public class RateLimitException extends Sec4DevException {

    private final int retryAfter;
    private final int limit;
    private final int remaining;

    public RateLimitException(String message, int statusCode, Object responseBody,
                              int retryAfter, int limit, int remaining) {
        super(message, statusCode, responseBody);
        this.retryAfter = retryAfter;
        this.limit = limit;
        this.remaining = remaining;
    }

    public int getRetryAfter() {
        return retryAfter;
    }

    public int getLimit() {
        return limit;
    }

    public int getRemaining() {
        return remaining;
    }
}
