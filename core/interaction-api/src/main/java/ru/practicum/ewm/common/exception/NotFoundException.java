package ru.practicum.ewm.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    static final String REASON = "The required object was not found.";

    public NotFoundException(String message) {
        super(REASON, message, HttpStatus.NOT_FOUND);
    }
}
