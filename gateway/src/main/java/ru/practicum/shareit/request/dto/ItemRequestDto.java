package ru.practicum.shareit.request.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
public final class ItemRequestDto {
    @NotEmpty
    private final String description;
    private final Long requesterId;
}
