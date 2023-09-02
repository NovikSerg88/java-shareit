package ru.practicum.shareit.exception;

import lombok.Generated;

@Generated
public class ValidationException extends IllegalArgumentException {
    public ValidationException(String message) {
        super(message);
    }
}
