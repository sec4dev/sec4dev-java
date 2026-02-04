package com.sec4dev.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** Result of an email check. */
public final class EmailCheckResult {

    private final String email;
    private final String domain;
    private final boolean disposable;

    public EmailCheckResult(@JsonProperty("email") String email,
                            @JsonProperty("domain") String domain,
                            @JsonProperty("is_disposable") boolean disposable) {
        this.email = email;
        this.domain = domain;
        this.disposable = disposable;
    }

    public String getEmail() {
        return email;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isDisposable() {
        return disposable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailCheckResult that = (EmailCheckResult) o;
        return disposable == that.disposable
                && Objects.equals(email, that.email)
                && Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, domain, disposable);
    }
}
