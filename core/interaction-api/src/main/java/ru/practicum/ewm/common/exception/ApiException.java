package ru.practicum.ewm.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import ru.practicum.ewm.common.ErrorResponse;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiException extends RuntimeException {
    final ErrorResponse errorResponse;

    public ApiException(String reason, String message, HttpStatus status) {
        super(message);
        this.errorResponse = ErrorResponse.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .sc(status)
                .build();
    }
}
