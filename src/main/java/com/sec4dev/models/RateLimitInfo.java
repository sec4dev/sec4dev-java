package com.sec4dev.models;

import java.util.Objects;

/** Rate limit info from response headers. */
public final class RateLimitInfo {

    private final int limit;
    private final int remaining;
    private final int resetSeconds;

    public RateLimitInfo(int limit, int remaining, int resetSeconds) {
        this.limit = limit;
        this.remaining = remaining;
        this.resetSeconds = resetSeconds;
    }

    public int getLimit() { return limit; }
    public int getRemaining() { return remaining; }
    public int getResetSeconds() { return resetSeconds; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateLimitInfo that = (RateLimitInfo) o;
        return limit == that.limit && remaining == that.remaining && resetSeconds == that.resetSeconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, remaining, resetSeconds);
    }
}
