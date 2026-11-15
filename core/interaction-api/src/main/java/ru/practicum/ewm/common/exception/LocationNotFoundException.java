package ru.practicum.ewm.common.exception;

public class LocationNotFoundException extends NotFoundException {
    public LocationNotFoundException(Long id) {
        super("Location with id=" + id + " was not found");
    }
}