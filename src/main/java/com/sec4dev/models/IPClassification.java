package com.sec4dev.models;

/** IP classification type. */
public enum IPClassification {
    HOSTING("hosting"),
    RESIDENTIAL("residential"),
    MOBILE("mobile"),
    VPN("vpn"),
    TOR("tor"),
    PROXY("proxy"),
    UNKNOWN("unknown");

    private final String value;

    IPClassification(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IPClassification fromValue(String v) {
        if (v == null) return UNKNOWN;
        for (IPClassification c : values()) {
            if (c.value.equals(v)) return c;
        }
        return UNKNOWN;
    }
}
