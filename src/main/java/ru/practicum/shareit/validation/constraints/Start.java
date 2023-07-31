package ru.practicum.shareit.validation.constraints;

import ru.practicum.shareit.validation.StartValidation;

import javax.validation.Constraint;
import javax.validation.constraints.Past;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartValidation.class)
@Target(ElementType.TYPE)
public @interface Start {
    String message() default "Start time is not valid";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};

}
