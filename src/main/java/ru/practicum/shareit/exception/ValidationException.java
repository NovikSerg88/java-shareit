package ru.practicum.shareit.exception;

import java.security.InvalidParameterException;

public class ValidationException extends IllegalArgumentException {
    public ValidationException(String s) {
        super(s);
    }
}
