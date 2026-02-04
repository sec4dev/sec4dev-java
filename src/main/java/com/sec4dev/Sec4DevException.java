package com.sec4dev;

/**
 * Base exception for all Sec4Dev API errors.
 */
public class Sec4DevException extends RuntimeException {

    private final int statusCode;
    private final Object responseBody;

    public Sec4DevException(String message, int statusCode, Object responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Object getResponseBody() {
        return responseBody;
    }
}
