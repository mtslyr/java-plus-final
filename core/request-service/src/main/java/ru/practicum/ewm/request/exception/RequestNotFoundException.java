package ru.practicum.ewm.request.exception;

import ru.practicum.ewm.common.exception.NotFoundException;

import java.util.Collection;

public class RequestNotFoundException extends NotFoundException {

    public RequestNotFoundException(Long id) {
        super("Request with ID = %d not found.".formatted(id));
    }

    public RequestNotFoundException(Collection<Long> ids) {
        super("Requests with IDs = %s not found.".formatted(ids));
    }
}
