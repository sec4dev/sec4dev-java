package com.sec4dev;

/** 404 - Resource not found. */
public class NotFoundException extends Sec4DevException {
    public NotFoundException(String message, int statusCode, Object responseBody) {
        super(message, statusCode, responseBody);
    }
}
