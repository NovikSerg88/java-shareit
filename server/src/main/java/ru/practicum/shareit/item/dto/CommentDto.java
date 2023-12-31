package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Generated
public final class CommentDto {
    private Long id;
    @NotEmpty
    @NotBlank
    private String text;
    private String authorName;
    private LocalDateTime created;
}