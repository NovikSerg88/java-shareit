package ru.practicum.shareit.validation;

import ru.practicum.shareit.validation.constraints.Start;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StartValidation implements ConstraintValidator<Start, LocalDateTime> {
    private LocalDateTime start;

    @Override
    public void initialize(Start constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDateTime start, ConstraintValidatorContext context) {

        return false;
    }
}
