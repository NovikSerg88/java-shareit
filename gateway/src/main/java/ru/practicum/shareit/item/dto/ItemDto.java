package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public final class ItemDto {
    private final Long id;
    private final Long ownerId;
    @NotEmpty
    private final String name;
    @NotEmpty
    private final String description;
    @NotNull
    private final Boolean available;
    private final Long requestId;
}