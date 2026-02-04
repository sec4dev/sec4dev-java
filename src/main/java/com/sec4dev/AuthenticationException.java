package com.sec4dev;

/** 401 - Invalid or missing API key. */
public class AuthenticationException extends Sec4DevException {
    public AuthenticationException(String message, int statusCode, Object responseBody) {
        super(message, statusCode, responseBody);
    }
}
