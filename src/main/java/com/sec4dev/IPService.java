package com.sec4dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sec4dev.models.*;

import java.util.Map;

/** IP check service. */
public final class IPService {

    private final com.sec4dev.HttpClient http;
    private final com.sec4dev.HttpClient.RateLimitCallback onRateLimit;
    private final ObjectMapper mapper = new ObjectMapper();

    IPService(com.sec4dev.HttpClient http, com.sec4dev.HttpClient.RateLimitCallback onRateLimit) {
        this.http = http;
        this.onRateLimit = onRateLimit;
    }

    /** Classify an IP address. */
    public IPCheckResult check(String ip) {
        Validation.validateIp(ip);
        String path = "/ip/check";
        Map<String, String> body = Map.of("ip", ip.trim());
        byte[] out = http.post(path, body, onRateLimit);
        try {
            return mapper.readValue(out, IPCheckResult.class);
        } catch (Exception e) {
            throw new Sec4DevException("Failed to parse response: " + e.getMessage(), 0, null);
        }
    }

    public boolean isHosting(String ip) {
        return check(ip).getSignals().isHosting();
    }

    public boolean isVpn(String ip) {
        return check(ip).getSignals().isVpn();
    }

    public boolean isTor(String ip) {
        return check(ip).getSignals().isTor();
    }

    public boolean isResidential(String ip) {
        return check(ip).getSignals().isResidential();
    }

    public boolean isMobile(String ip) {
        return check(ip).getSignals().isMobile();
    }
}
