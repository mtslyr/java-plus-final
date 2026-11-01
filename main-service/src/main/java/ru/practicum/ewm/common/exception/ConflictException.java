package ru.practicum.ewm.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
    static final String REASON = "Integrity constraint has been violated.";

    public ConflictException(String message) {
        super(REASON, message, HttpStatus.CONFLICT);
    }
}
