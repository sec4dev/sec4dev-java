package com.sec4dev.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** Network info from an IP check. */
public final class IPNetwork {

    private final Integer asn;
    private final String org;
    private final String provider;

    public IPNetwork(@JsonProperty("asn") Integer asn,
                     @JsonProperty("org") String org,
                     @JsonProperty("provider") String provider) {
        this.asn = asn;
        this.org = org;
        this.provider = provider;
    }

    public Integer getAsn() { return asn; }
    public String getOrg() { return org; }
    public String getProvider() { return provider; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPNetwork that = (IPNetwork) o;
        return Objects.equals(asn, that.asn) && Objects.equals(org, that.org)
                && Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asn, org, provider);
    }
}
