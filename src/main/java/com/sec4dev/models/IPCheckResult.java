package com.sec4dev.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** Result of an IP check. */
public final class IPCheckResult {

    private final String ip;
    private final String classification;
    private final double confidence;
    private final IPSignals signals;
    private final IPNetwork network;
    private final IPGeo geo;

    public IPCheckResult(@JsonProperty("ip") String ip,
                         @JsonProperty("classification") String classification,
                         @JsonProperty("confidence") double confidence,
                         @JsonProperty("signals") IPSignals signals,
                         @JsonProperty("network") IPNetwork network,
                         @JsonProperty("geo") IPGeo geo) {
        this.ip = ip;
        this.classification = classification != null ? classification : "unknown";
        this.confidence = confidence;
        this.signals = signals != null ? signals : new IPSignals(false, false, false, false, false, false);
        this.network = network != null ? network : new IPNetwork(null, null, null);
        this.geo = geo != null ? geo : new IPGeo(null, null);
    }

    public String getIp() { return ip; }
    public String getClassification() { return classification; }
    public double getConfidence() { return confidence; }
    public IPSignals getSignals() { return signals; }
    public IPNetwork getNetwork() { return network; }
    public IPGeo getGeo() { return geo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPCheckResult that = (IPCheckResult) o;
        return Double.compare(that.confidence, confidence) == 0
                && Objects.equals(ip, that.ip) && Objects.equals(classification, that.classification)
                && Objects.equals(signals, that.signals) && Objects.equals(network, that.network)
                && Objects.equals(geo, that.geo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, classification, confidence, signals, network, geo);
    }
}
