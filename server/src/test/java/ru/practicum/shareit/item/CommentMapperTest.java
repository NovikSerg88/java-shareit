package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommentMapperTest {

    private Long userId;
    private Long itemId;
    private Long requestId;
    private Long commentId;
    private User user;
    private Item item;
    private ItemDto itemDto;
    private final ItemMapper itemMapper = new ItemMapper();
    private ItemRequest itemRequest;
    private Comment comment;
    private CommentRequest commentRequest;
    private final CommentMapper commentMapper = new CommentMapper();

    @BeforeEach
    void setUp() {
        userId = 1L;
        itemId = 2L;
        requestId = 3L;
        commentId = 4L;
        itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("request description")
                .requester(user)
                .created(LocalDateTime.now())
                .build();
        user = new User(userId, "user", "user@mail.ru");
        item = new Item(itemId, user, "name", "description", true, itemRequest, null);
        itemDto = new ItemDto(itemId, user.getId(), "name", "description", true, itemRequest.getId(), null, null, null);
        comment = new Comment(commentId, "comment", item, user, LocalDateTime.now());
        commentRequest = new CommentRequest("comment", userId, itemId);
    }

    @Test
    public void testMapToDto() {

        CommentResponse response = commentMapper.mapToDto(comment);

        assertNotNull(response);
        assertEquals(comment.getId(), response.getId());
        assertEquals(comment.getText(), response.getText());
        assertEquals(user.getName(), response.getAuthorName());
        assertEquals(comment.getCreated(), response.getCreated());
    }

    @Test
    public void testMapToComment() {

        Comment comment = commentMapper.mapToComment(commentRequest, user, item);

        assertNotNull(comment);
        assertEquals(commentRequest.getText(), comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(user, comment.getAuthor());
        assertNotNull(comment.getCreated());
    }
}





