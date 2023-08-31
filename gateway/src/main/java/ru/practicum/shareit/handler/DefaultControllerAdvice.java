package ru.practicum.shareit.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DefaultControllerAdvice {
    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(GatewayException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .code(ex.getCode())
                .error(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getCode()));
    }
}
