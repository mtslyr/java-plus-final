package ru.practicum.ewm.common.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApiException {
    static final String REASON = "Incorrectly made request.";

    public ValidationException(String message) {
        super(REASON, message, HttpStatus.BAD_REQUEST);
    }
}
