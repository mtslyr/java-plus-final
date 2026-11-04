package ru.practicum.ewm.event.exception;

import ru.practicum.ewm.common.exception.NotFoundException;

public class EventNotFoundException extends NotFoundException {
    public EventNotFoundException(Long id) {
        super("Event with ID = %d not found".formatted(id));
    }
}
