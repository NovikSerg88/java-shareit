package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CommentRequest {
    @NotEmpty
    private String text;
    private Long userId;
    private Long itemId;
}