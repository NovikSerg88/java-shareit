package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private RequestMapper requestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private Long userId;
    private ItemRequestDto requestDto;
    private User user;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private ItemRequestResponseDto itemRequestResponseDto;
    private Long requestId;
    private Item item;
    private Long itemId;
    private List<ItemResponseDto> items = new ArrayList<>();
    private ItemResponseDto itemResponseDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        requestId = 1L;
        itemId = 1L;
        user = new User(userId, "user", "user@mail.ru");
        item = new Item(itemId, user, "name", "description", true, itemRequest, null);
        itemResponseDto = new ItemResponseDto(1L, "response", "description", true, requestId);
        items.add(itemResponseDto);
        itemRequestResponseDto = new ItemRequestResponseDto(1L, "description", LocalDateTime.now(), items);
        itemRequest = new ItemRequest(1L, "description", user, LocalDateTime.now());
    }

    @Test
    void createRequestWhenUserNotFoundThenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.create(requestDto, userId));

        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    public void createItemRequestAndReturn() {
        ItemRequestDto itemRequestDto = requestDto;
        User mockUser = user;
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));

        ItemRequest mockItemRequest = itemRequest;
        when(requestMapper.toItemRequest(eq(itemRequestDto), eq(mockUser))).thenReturn(mockItemRequest);

        ItemRequestResponseDto mockResponseDto = itemRequestResponseDto;
        when(requestMapper.toResponseDto(mockItemRequest)).thenReturn(mockResponseDto);
        when(itemRequestRepository.save(mockItemRequest)).thenReturn(mockItemRequest);

        ItemRequestResponseDto resultDto = itemRequestService.create(itemRequestDto, 1L);
        assertEquals(mockResponseDto, resultDto);

        verify(userRepository).findById(1L);
        verify(itemRequestRepository).save(mockItemRequest);
    }

    @Test
    void getAllUserRequestsAndThrowIfUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllUserRequests(userId));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void getAllUserRequestsAndReturn() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<ItemRequest> requests = new ArrayList<>();
        requests.add(itemRequest);
        when(itemRequestRepository.findAllByRequesterId(userId)).thenReturn(requests);

        List<Item> items = new ArrayList<>();
        items.add(item);
        when(itemRepository.findAllByRequestId(itemRequest.getId())).thenReturn(items);
        when(requestMapper.mapToItemResponse(eq(item), anyLong())).thenReturn(itemResponseDto);

        when(requestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);
        List<ItemRequestResponseDto> result = itemRequestService.getAllUserRequests(userId);

        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).findAllByRequesterId(userId);
        verify(itemRepository, times(1)).findAllByRequestId(itemRequest.getId());
        verify(requestMapper, times(1)).mapToItemResponse(eq(item), anyLong());
        verify(requestMapper, times(1)).toResponseDto(itemRequest);
    }

    @Test
    public void getAllRequestsAndReturn() {
        int from = 0;
        int size = 10;

        List<ItemRequest> itemRequests = new ArrayList<>();
        itemRequests.add(itemRequest);
        Page<ItemRequest> page = new PageImpl<>(itemRequests);

        when(itemRequestRepository.findAllByRequesterIdNot(eq(userId), any(PageRequest.class))).thenReturn(page);

        List<Item> items = new ArrayList<>();
        items.add(item);
        when(itemRepository.findAllByRequestId(itemRequest.getId())).thenReturn(items);

        when(requestMapper.mapToItemResponse(eq(item), anyLong())).thenReturn(itemResponseDto);

        when(requestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(userId, from, size);

        assertEquals(1, result.size());

        verify(itemRequestRepository, times(1)).findAllByRequesterIdNot(eq(userId), any(PageRequest.class));
        verify(itemRepository, times(1)).findAllByRequestId(itemRequest.getId());
        verify(requestMapper, times(1)).mapToItemResponse(eq(item), anyLong());
        verify(requestMapper, times(1)).toResponseDto(itemRequest);
    }

    @Test
    public void getRequestByIdAndReturn() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));

        List<Item> items = new ArrayList<>();
        items.add(item);

        when(itemRepository.findAllByRequestId(requestId)).thenReturn(items);

        when(requestMapper.mapToItemResponse(eq(item), anyLong())).thenReturn(itemResponseDto);

        when(requestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);

        ItemRequestResponseDto result = itemRequestService.getRequestById(requestId, userId);

        assertNotNull(result);

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).findById(requestId);
        verify(itemRepository, times(1)).findAllByRequestId(requestId);
        verify(requestMapper, times(1)).mapToItemResponse(eq(item), anyLong());
        verify(requestMapper, times(1)).toResponseDto(itemRequest);
    }

    @Test
    void getRequestByIdAndThrowIfUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(requestId, userId));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getRequestByIdAndThrowIfItemNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(requestId, userId));

        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }
}
