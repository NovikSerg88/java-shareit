package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@Generated
public final class CommentRequest {
    @NotEmpty
    private String text;
    private Long userId;
    private Long itemId;
}