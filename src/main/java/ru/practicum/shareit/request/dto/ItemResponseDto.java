package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private boolean available;
    private Long requestId;
}
