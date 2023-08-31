package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    public CommentResponse mapToDto(Comment domain) {
        return CommentResponse.builder()
                .id(domain.getId())
                .text(domain.getText())
                .authorName(domain.getAuthor().getName())
                .created(domain.getCreated())
                .build();
    }

    public Comment mapToComment(CommentRequest dto, User user, Item item) {
        return Comment.builder()
                .text(dto.getText())
                .item(item)
                .created(LocalDateTime.now())
                .author(user)
                .build();
    }
}