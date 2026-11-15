package ru.practicum.ewm.common.exception;

import ru.practicum.ewm.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long id) {
        super("User with ID = %d not found.".formatted(id));
    }
}
