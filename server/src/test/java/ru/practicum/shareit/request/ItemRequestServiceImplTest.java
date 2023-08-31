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
import org.springframework.data.domain.Sort;
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

        when(itemRequestRepository.findAllByRequesterId(userId)).thenReturn(Collections.singletonList(itemRequest));

        List<Item> items = Collections.singletonList(item);
        when(itemRepository.findAll()).thenReturn(items);

        when(requestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);

        List<ItemRequestResponseDto> result = itemRequestService.getAllUserRequests(userId);

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getItems());
    }

    @Test
    public void getAllRequestsAndReturnEmpty() {
        int from = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"));

        Page<ItemRequest> itemRequests = new PageImpl<>(Collections.emptyList());
        when(itemRequestRepository.findAllByRequesterIdNot(userId, pageRequest))
                .thenReturn(itemRequests);

        List<Item> items = Collections.singletonList(item);
        when(itemRepository.findAll()).thenReturn(items);

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(userId, from, size);

        assertEquals(0, result.size());
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

    @Test
    public void getAllUserRequestsWithEmptyItemsAndReturn() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(itemRequestRepository.findAllByRequesterId(userId)).thenReturn(Collections.singletonList(itemRequest));

        List<Item> items = Collections.singletonList(item);
        when(itemRepository.findAll()).thenReturn(items);

        when(requestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);

        List<ItemRequestResponseDto> result = itemRequestService.getAllUserRequests(userId);

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getItems());
    }

    @Test
    public void getAllUserRequestsWithNoRequestsAndReturnEmpty() {
        User testUser = new User();
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRequestRepository.findAllByRequesterId(userId)).thenReturn(Collections.emptyList());

        List<ItemRequestResponseDto> result = itemRequestService.getAllUserRequests(userId);

        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).findAllByRequesterId(userId);
    }

    @Test
    public void getAllUserRequestsWithItemsAssociatedWithRequests() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterId(userId)).thenReturn(Collections.singletonList(itemRequest));
        when(requestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);
        when(itemRepository.findAll()).thenReturn(Collections.singletonList(item));

        List<ItemRequestResponseDto> result = itemRequestService.getAllUserRequests(userId);

        assertFalse(result.isEmpty());

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).findAllByRequesterId(userId);
        verify(requestMapper, times(1)).toResponseDto(itemRequest);
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void getAllUserRequestsAndReturnWithItems() {
        Long userId = 1L;
        int from = 0;
        int size = 10;

        ItemRequest request1 = new ItemRequest(1L, "description1", user, LocalDateTime.now());
        List<ItemRequest> mockRequests = List.of(request1);

        Item item1 = new Item(1L, user, "name1", "description1", true, request1, null);
        List<Item> mockItems = List.of(item1);

        PageRequest pageRequest = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"));
        Page<ItemRequest> mockPage = new PageImpl<>(mockRequests, pageRequest, mockRequests.size());

        when(itemRequestRepository.findAllByRequesterIdNot(userId, pageRequest)).thenReturn(mockPage);
        when(itemRepository.findAll()).thenReturn(mockItems);

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto(1L, "description1", LocalDateTime.now(), items);
        List<ItemRequestResponseDto> expected = new ArrayList<>();
        for (ItemRequest request : mockPage) {
            when(requestMapper.toResponseDto(request)).thenReturn(responseDto);
            ItemRequestResponseDto dto = requestMapper.toResponseDto(request);
            List<ItemResponseDto> itemResponseDtos = new ArrayList<>();
            for (Item item : mockItems) {
                if (item.getRequest() != null && item.getRequest().getId().equals(request.getId())) {
                    when(requestMapper.mapToItemResponse(item, request.getId())).thenReturn(itemResponseDto);
                    itemResponseDtos.add(itemResponseDto);
                }
            }
            dto.setItems(itemResponseDtos);
            expected.add(dto);
        }

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(userId, from, size);

        assertEquals(1, result.size());
        assertEquals(expected, result);

        verify(itemRequestRepository, times(1)).findAllByRequesterIdNot(userId, pageRequest);
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void getAllRequestsAndReturnWithItems() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemRequest request1 = new ItemRequest(1L, "description1", user, LocalDateTime.now());

        List<ItemRequest> mockRequests = List.of(request1);

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto(1L, "description1", LocalDateTime.now(), items);
        List<ItemRequestResponseDto> mockResponseDtos = new ArrayList<>();

        when(itemRequestRepository.findAllByRequesterId(userId)).thenReturn(mockRequests);

        for (ItemRequest request : mockRequests) {
            when(requestMapper.toResponseDto(request)).thenReturn(responseDto);
            mockResponseDtos.add(responseDto);
        }

        Item item1 = new Item(1L, user, "name1", "description1", true, request1, null);
        List<Item> mockItems = List.of(item1);

        when(itemRepository.findAll()).thenReturn(mockItems);

        List<ItemResponseDto> itemResponseDtos = new ArrayList<>();
        List<ItemRequestResponseDto> expected = new ArrayList<>();

        for (ItemRequestResponseDto dto : mockResponseDtos) {
            for (Item item : mockItems) {
                if (item.getRequest() != null && item.getRequest().getId().equals(dto.getId())) {
                    when(requestMapper.mapToItemResponse(item, dto.getId())).thenReturn(itemResponseDto);
                    itemResponseDtos.add(itemResponseDto);
                }
            }
            dto.setItems(itemResponseDtos);
            expected.add(dto);
        }

        List<ItemRequestResponseDto> result = itemRequestService.getAllUserRequests(userId);

        assertEquals(1, expected.size());
        assertEquals(expected, result);

        verify(itemRequestRepository, times(1)).findAllByRequesterId(userId);
        verify(itemRepository, times(1)).findAll();
    }
}
