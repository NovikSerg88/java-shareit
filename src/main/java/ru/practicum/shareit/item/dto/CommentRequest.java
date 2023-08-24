package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
public final class CommentRequest {
    @NotEmpty
    private String text;
    private Long userId;
    private Long itemId;
}