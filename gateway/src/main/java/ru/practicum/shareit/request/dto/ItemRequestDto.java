package ru.practicum.shareit.request.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
public final class ItemRequestDto {
    @NotEmpty
    private String description;
    private Long requesterId;
}
