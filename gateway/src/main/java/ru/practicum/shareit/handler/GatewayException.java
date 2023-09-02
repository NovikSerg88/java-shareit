package ru.practicum.shareit.handler;

import lombok.Getter;

public class GatewayException extends RuntimeException {
    @Getter
    private final int code;

    public GatewayException(int code, String message) {
        super(message);
        this.code = code;
    }
}
