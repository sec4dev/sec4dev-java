package com.sec4dev;

/** 422 - Invalid input or client-side validation failure. */
public class ValidationException extends Sec4DevException {
    public ValidationException(String message, int statusCode, Object responseBody) {
        super(message, statusCode, responseBody);
    }
}
