package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Generated
public final class CommentDto {
    private final Long id;
    @NotEmpty
    @NotBlank
    private final String text;
    private final String authorName;
    private final LocalDateTime created;
}