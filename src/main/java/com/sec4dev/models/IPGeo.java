package com.sec4dev.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** Geo info from an IP check. */
public final class IPGeo {

    private final String country;
    private final String region;

    public IPGeo(@JsonProperty("country") String country,
                 @JsonProperty("region") String region) {
        this.country = country;
        this.region = region;
    }

    public String getCountry() { return country; }
    public String getRegion() { return region; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPGeo that = (IPGeo) o;
        return Objects.equals(country, that.country) && Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, region);
    }
}
