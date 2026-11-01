package ru.practicum.ewm.comment.exception;

import ru.practicum.ewm.common.exception.NotFoundException;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(Long id) {
        super("Comment with ID = %d not found.".formatted(id));
    }
}
