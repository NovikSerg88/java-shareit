package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ItemMapperTest {

    private Long userId;
    private Long itemId;
    private Long requestId;
    private User user;
    private Item item;
    private ItemDto itemDto;
    private final ItemMapper itemMapper = new ItemMapper();
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        userId = 1L;
        itemId = 2L;
        requestId = 3L;
        itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("request description")
                .requester(user)
                .created(LocalDateTime.now())
                .build();
        user = new User(userId, "user", "user@mail.ru");
        item = new Item(itemId, user, "name", "description", true, itemRequest, null);
        itemDto = new ItemDto(itemId, user.getId(), "name", "description", true, itemRequest.getId(), null, null, null);
    }

    @Test
    public void testMapToDto() {

        ItemDto itemDto = itemMapper.mapToDto(item);

        assertNotNull(itemDto);
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(user.getId(), itemDto.getOwnerId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
    }

    @Test
    public void testMapToItem() {

        Item item = itemMapper.mapToItem(itemDto, user, itemRequest);

        assertNotNull(item);
        assertEquals(itemDto.getId(), item.getId());
        assertEquals(itemDto.getName(), item.getName());
        assertEquals(itemDto.getDescription(), item.getDescription());
        assertEquals(user, item.getOwner());
        assertTrue(item.getAvailable());
    }
}
