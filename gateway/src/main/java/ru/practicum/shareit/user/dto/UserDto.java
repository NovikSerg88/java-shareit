package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@Setter
public final class UserDto {
    private final Long id;
    @NotBlank
    private final String name;
    @Email
    @NotBlank
    private final String email;
}
