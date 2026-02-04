package com.sec4dev;

import java.net.InetAddress;
import java.util.regex.Pattern;

/** Client-side input validation. */
public final class Validation {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private Validation() {}

    /** Validates email format. Throws ValidationException if invalid. */
    public static void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new ValidationException("Email is required", 422, null);
        }
        String s = email.trim();
        if (s.isEmpty()) {
            throw new ValidationException("Email cannot be empty", 422, null);
        }
        if (!EMAIL_PATTERN.matcher(s).matches()) {
            throw new ValidationException("Invalid email format", 422, null);
        }
    }

    /** Validates IP (IPv4 or IPv6). Throws ValidationException if invalid. */
    public static void validateIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            throw new ValidationException("IP address is required", 422, null);
        }
        String s = ip.trim();
        if (s.isEmpty()) {
            throw new ValidationException("IP address cannot be empty", 422, null);
        }
        try {
            InetAddress.getByName(s);
        } catch (Exception e) {
            throw new ValidationException("Invalid IP address format", 422, null);
        }
    }
}
