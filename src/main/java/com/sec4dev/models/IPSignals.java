package com.sec4dev.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** Signals from an IP check. */
public final class IPSignals {

    private final boolean hosting;
    private final boolean residential;
    private final boolean mobile;
    private final boolean vpn;
    private final boolean tor;
    private final boolean proxy;

    public IPSignals(@JsonProperty("is_hosting") boolean hosting,
                     @JsonProperty("is_residential") boolean residential,
                     @JsonProperty("is_mobile") boolean mobile,
                     @JsonProperty("is_vpn") boolean vpn,
                     @JsonProperty("is_tor") boolean tor,
                     @JsonProperty("is_proxy") boolean proxy) {
        this.hosting = hosting;
        this.residential = residential;
        this.mobile = mobile;
        this.vpn = vpn;
        this.tor = tor;
        this.proxy = proxy;
    }

    public boolean isHosting() { return hosting; }
    public boolean isResidential() { return residential; }
    public boolean isMobile() { return mobile; }
    public boolean isVpn() { return vpn; }
    public boolean isTor() { return tor; }
    public boolean isProxy() { return proxy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPSignals that = (IPSignals) o;
        return hosting == that.hosting && residential == that.residential
                && mobile == that.mobile && vpn == that.vpn && tor == that.tor && proxy == that.proxy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hosting, residential, mobile, vpn, tor, proxy);
    }
}
