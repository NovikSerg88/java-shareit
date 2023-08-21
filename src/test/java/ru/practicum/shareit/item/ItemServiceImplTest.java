package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;

    @Mock
    ItemMapper itemMapper;

    @Mock
    UserRepository userRepository;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @Mock
    CommentService commentService;

    @InjectMocks
    ItemServiceImpl itemServiceImpl;

    private UserDto userDto;
    private User user;
    private ItemRequest itemRequest;
    private ItemDto itemDto;
    private Item item;
    private Booking booking;
    private LocalDateTime current;
    private List<Booking> bookings;

    @BeforeEach
    void setUp() {
        bookings = new ArrayList<>();
        current = LocalDateTime.now();
        userDto = new UserDto(1L, "user", "user@user.ru");
        user = new User(1L, "user", "user@user.ru");
        itemRequest = new ItemRequest(1L, "description 1", user, LocalDateTime.now());
        item = new Item(1L, user, "item", "description", true, itemRequest, bookings);
        itemDto = new ItemDto(1L, userDto.getId(), "item", "description",
                true, 1L, null, null, null);
        booking = new Booking(1L, current, current.plusHours(1), item, user, Status.WAITING);
    }

    @Test
    void findItemByIdAndThrowIfNotFound() {
        when(itemRepository.findItemByIdWithBookingsFetched(anyLong())).thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemServiceImpl.findById(itemDto.getId(), userDto.getId()));

        assertEquals("Item not found.",
                exception.getMessage());
        verify(itemRepository).findItemByIdWithBookingsFetched(itemDto.getId());
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    public void findByIdAndReturnItem() {
        bookings.add(booking);

        List<CommentResponse> mockComments = new ArrayList<>();

        when(itemRepository.findItemByIdWithBookingsFetched(item.getId())).thenReturn(java.util.Optional.of(item));
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(commentService.getCommentsOfItem(item.getId())).thenReturn(mockComments);

        ItemDto resultDto = itemServiceImpl.findById(item.getId(), user.getId());

        verify(itemRepository).findItemByIdWithBookingsFetched(item.getId());
        verify(itemMapper).mapToDto(item);
        verify(commentService).getCommentsOfItem(item.getId());

        assertEquals(itemDto, resultDto);
    }

    @Test
    void updateItemAndThrowIfItemNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemServiceImpl.update(itemDto, userDto.getId(), itemDto.getId()));

        assertEquals("Item not found.",
                exception.getMessage());
        verify(itemRepository).findById(itemDto.getId());
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    public void updateItemDoesNotBelongToUser() {
        Long ownerId = 1L;
        Long itemId = 2L;
        ItemDto dto = itemDto;

        User fakeOwner = user;
        fakeOwner.setId(ownerId);

        User fakeItemOwner = user;
        fakeItemOwner.setId(3L);

        Item fakeItem = item;
        fakeItem.setOwner(fakeItemOwner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(fakeItem));
        when(userRepository.getReferenceById(ownerId)).thenReturn(fakeOwner);

        assertThrows(NotFoundException.class, () -> {
            itemServiceImpl.update(dto, ownerId, itemId);
        });
    }

    @Test
    public void updateItemAndReturnUpdated() {
        Long ownerId = 1L;
        Long itemId = 2L;

        User owner = user;
        owner.setId(ownerId);

        item.setId(itemId);
        item.setOwner(owner);

        when(userRepository.getReferenceById(ownerId)).thenReturn(owner);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        when(itemMapper.mapToDto(item)).thenReturn(itemDto);

        ItemDto result = itemServiceImpl.update(itemDto, ownerId, itemId);

        verify(userRepository, times(1)).getReferenceById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(item);
        verify(itemMapper, times(1)).mapToDto(item);

        assertEquals(itemDto, result);
    }

    @Test
    void saveItemAndReturnItemDtoIfFound() {
        Long userId = 1L;
        Long requestId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));

        itemDto = new ItemDto(1L, userDto.getId(), "item", "description",
                true, 1L, null, null, null);
        itemDto.setRequestId(requestId);

        Item mappedItem = new Item();
        when(itemMapper.mapToItem(itemDto, user, itemRequest)).thenReturn(mappedItem);

        Item savedItem = new Item();
        when(itemRepository.save(mappedItem)).thenReturn(savedItem);

        when(itemMapper.mapToDto(savedItem)).thenReturn(itemDto);

        ItemDto result = itemServiceImpl.saveItem(itemDto, userId);

        assertEquals(itemDto, result);
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findById(requestId);
        verify(itemMapper).mapToItem(itemDto, user, itemRequest);
        verify(itemRepository).save(mappedItem);
        verify(itemMapper).mapToDto(savedItem);
    }

    @Test
    void saveItemAndThrowsIfUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemServiceImpl.saveItem(itemDto, userDto.getId()));

        assertEquals("User not found.",
                exception.getMessage());
        verify(userRepository).findById(user.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void saveItemAndThrowsIfRequestNotFound() {
        Long requestId = 1L;
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemServiceImpl.saveItem(itemDto, userDto.getId()));

        assertEquals("Request not found",
                exception.getMessage());
        verify(itemRequestRepository).findById(requestId);
        verifyNoMoreInteractions(itemRequestRepository);
    }

    @Test
    void getItemsOfUser() {
        long userId = 1L;
        int from = 0;
        int size = 10;

        PageRequest pageRequest = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "name"));

        List<Item> items = new ArrayList<>();
        when(itemRepository.findAllByOwnerIdFetchBookings(userId, pageRequest)).thenReturn(items);

        Map<Long, List<CommentResponse>> itemIdToComments = new HashMap<>();
        when(commentService.getItemIdToComments(any())).thenReturn(itemIdToComments);

        List<ItemDto> expectedDtos = new ArrayList<>();
        ItemServiceImpl spyItemService = spy(itemServiceImpl);

        List<ItemDto> result = spyItemService.getItemsForUser(userId, from, size);

        assertEquals(expectedDtos, result);
        verify(itemRepository).findAllByOwnerIdFetchBookings(userId, pageRequest);
        verify(commentService).getItemIdToComments(anySet());

        for (Item item : items) {
            verify(itemMapper).mapToDto(item);
        }
    }

    @Test
    public void searchAvailableItemsWithEmptyQuery() {
        String query = "";
        int from = 0;
        int size = 10;

        List<ItemDto> result = itemServiceImpl.searchAvailableItems(query, from, size);

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAllItemFetchOwnerByQuery(any(), any());
        verify(itemMapper, never()).mapToDto(any());
    }

    @Test
    public void testSearchAvailableItemsWithZeroSizeThrowsException() {
        int from = 0;
        int size = 0;
        String query = "sample query";

        assertThrows(IllegalArgumentException.class, () -> itemServiceImpl.searchAvailableItems(query, from, size));

        verify(itemRepository, never()).searchAllItemFetchOwnerByQuery(any(), any());
        verify(itemMapper, never()).mapToDto(any());
    }

    @Test
    public void searchAvailableItemsWithNonEmptyQuery() {
        int from = 0;
        int size = 10;
        String query = "sample query";

        List<Item> itemList = Collections.singletonList(item);
        List<ItemDto> dtoList = Collections.singletonList(itemDto);

        when(itemRepository.searchAllItemFetchOwnerByQuery(eq(query), any(PageRequest.class))).thenReturn(itemList);

        when(itemMapper.mapToDto(any())).thenReturn(dtoList.get(0));

        List<ItemDto> result = itemServiceImpl.searchAvailableItems(query, from, size);

        verify(itemRepository, times(1)).searchAllItemFetchOwnerByQuery(eq(query), any(PageRequest.class));
        verify(itemMapper, times(1)).mapToDto(any());

        assertEquals(dtoList, result);
    }

    @Test
    void deleteItemsForUser() {
        long userId = 1L;

        itemServiceImpl.deleteItemsForUser(userId);

        verify(itemRepository).deleteAllByOwner_Id(userId);
    }

    @Test
    void deleteItem() {
        long userId = 1L;
        long itemId = 2L;

        itemServiceImpl.deleteItem(userId, itemId);

        verify(itemRepository).deleteItemByIdAndOwner_Id(userId, itemId);
    }
}
