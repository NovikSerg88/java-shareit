package shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class RequestMapperTest {

    private Long userId;
    private Long itemId;
    private Long requestId;
    private User user;
    private Item item;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private final RequestMapper requestMapper = new RequestMapper();

    @BeforeEach
    void setUp() {
        userId = 1L;
        itemId = 2L;
        requestId = 3L;
        user = new User(userId, "user", "user@mail.ru");
        itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("request description")
                .requester(user)
                .created(LocalDateTime.now())
                .build();
        itemRequestDto = ItemRequestDto.builder()
                .description("request description")
                .requesterId(user.getId())
                .build();
        item = new Item(itemId, user, "name", "description", true, itemRequest, null);
    }

    @Test
    public void testMapToItemRequest() {

        ItemRequest itemRequest = requestMapper.toItemRequest(itemRequestDto, user);

        assertNotNull(itemRequest);
        assertEquals(itemRequestDto.getDescription(), itemRequest.getDescription());
        assertEquals(itemRequestDto.getRequesterId(), itemRequest.getRequester().getId());
    }

    @Test
    public void testMapToItemResponseDto() {

        ItemRequestResponseDto itemRequestResponseDto = requestMapper.toResponseDto(itemRequest);

        assertNotNull(itemRequestResponseDto);
        assertEquals(itemRequestResponseDto.getId(), itemRequest.getId());
        assertEquals(itemRequestResponseDto.getDescription(), itemRequest.getDescription());
        assertEquals(itemRequestResponseDto.getCreated(), itemRequest.getCreated());
    }

    @Test
    public void testMapToItemResponse() {

        ItemResponseDto itemResponseDto = requestMapper.mapToItemResponse(item, requestId);

        assertNotNull(itemResponseDto);
        assertEquals(itemResponseDto.getId(), item.getId());
        assertEquals(itemResponseDto.getName(), item.getName());
        assertEquals(itemResponseDto.getDescription(), item.getDescription());
        assertEquals(itemResponseDto.isAvailable(), item.getAvailable());
        assertEquals(itemResponseDto.getRequestId(), item.getRequest().getId());
    }
}
