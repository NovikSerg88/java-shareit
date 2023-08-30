package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public final class ItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private boolean available;
    private Long requestId;
}
