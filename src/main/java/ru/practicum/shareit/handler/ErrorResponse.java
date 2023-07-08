package ru.practicum.shareit.handler;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ErrorResponse {
    String error;

    public String getError() {
        return error;
    }
}
