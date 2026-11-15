package ru.practicum.ewm.category.exception;

import ru.practicum.ewm.common.exception.NotFoundException;

public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(Long id) {
        super("Катгория с ID = %d не найдена".formatted(id));
    }
}
