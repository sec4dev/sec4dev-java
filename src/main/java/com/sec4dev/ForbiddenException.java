package com.sec4dev;

/** 403 - Account deactivated. */
public class ForbiddenException extends Sec4DevException {
    public ForbiddenException(String message, int statusCode, Object responseBody) {
        super(message, statusCode, responseBody);
    }
}
