package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookerResponseDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.ItemResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingMapper bookingMapper;
    @InjectMocks
    private BookingServiceImpl bookingService;

    Long bookerId;
    Long itemId;
    Long userId;
    Long bookingId;
    int from;
    int size;
    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;
    private LocalDateTime current;
    private Item item;
    private User user;
    private User booker;
    private Long requestId;
    private ItemRequest itemRequest;
    private Booking booking;
    private ItemResponseDto itemResponseDto;
    private BookerResponseDto bookerResponseDto;

    @BeforeEach
    void setUp() {
        bookerId = 1L;
        itemId = 2L;
        userId = 3L;
        requestId = 4L;
        bookingId = 5L;
        from = 0;
        size = 10;
        current = LocalDateTime.now();
        bookingRequestDto = new BookingRequestDto(itemId, current, current.plusHours(1));
        user = new User(userId, "user", "user@mail.ru");
        booker = new User(bookerId, "booker", "booker@mail.ru");
        itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("request description")
                .requester(user)
                .created(LocalDateTime.now())
                .build();
        item = new Item(itemId, user, "name", "description", true, itemRequest, null);
        booking = new Booking(bookingId, current, current.plusHours(1), item, booker, Status.WAITING);
        bookingResponseDto = new BookingResponseDto(booking.getId(), current, current.plusHours(1),
                Status.WAITING, bookerResponseDto, itemResponseDto);
        bookerResponseDto = new BookerResponseDto(bookerId);
        itemResponseDto = new ItemResponseDto(item.getId(), item.getName());
    }

    @Test
    void createBookingWhenUserNotFoundThenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingRequestDto, bookerId));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingWhenUserAndItemFoundReturnBooking() {
        BookingResponseDto expected = new BookingResponseDto(booking.getId(), current, current.plusHours(1),
                Status.WAITING, bookerResponseDto, itemResponseDto);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        when(bookingMapper.mapToBooking(bookingRequestDto, item, booker)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.mapToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.create(bookingRequestDto, bookerId);

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(expected.getId());
    }

    @Test
    void getBookingByIdWhenUserIsOwnerReturnsBooking() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.mapToBookingResponseDto(booking)).thenReturn(bookingResponseDto);
        BookingResponseDto actual = bookingService.getBookingById(bookingId, userId);
        assertThat(actual).isEqualTo(bookingResponseDto);
    }

    @Test
    void getBookingByIdWhenUserIsNeitherBookerNorOwnerThrows() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(bookingId, userId + 100));
    }

    @Test
    void updateWhenRejectAndBookingNotRejectedReturnsRejectedBooking() {
        BookingResponseDto expectedResponse = new BookingResponseDto(booking.getId(), current, current.plusHours(1),
                Status.REJECTED, bookerResponseDto, itemResponseDto);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.mapToBookingResponseDto(booking)).thenReturn(expectedResponse);

        BookingResponseDto actualResponse = bookingService.update(bookingId, userId, false);

        assertEquals(expectedResponse, actualResponse);
        assertEquals(Status.REJECTED, booking.getStatus());
    }

    @Test
    void updateWhenRejectAndBookingRejectedThrowValidationException() {
        booking.setStatus(Status.REJECTED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class,
                () -> bookingService.update(bookingId, userId, false));
    }

    @Test
    public void getBookingsOfUserWhenStateIsAll() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        when(bookingRepository.findByBookerId(userId, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfUser(userId, State.ALL, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfUserWhenStateIsCurrent() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        when(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(
                userId, now, now, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfUser(userId, State.CURRENT, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfUserWhenStateIsPast() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        when(bookingRepository.findByBookerIdAndEndIsBefore(
                userId, now, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfUser(userId, State.PAST, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfUserWhenStateIsFuture() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        when(bookingRepository.findByBookerIdAndStartIsAfter(
                userId, now, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfUser(userId, State.FUTURE, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfUserWhenStateIsWaiting() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        when(bookingRepository.findByBookerIdAndStatus(
                userId, Status.WAITING, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfUser(userId, State.WAITING, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfUserWhenStateIsRejected() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        when(bookingRepository.findByBookerIdAndStatus(
                userId, Status.REJECTED, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfUser(userId, State.REJECTED, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfOwnerWhenStateIsAll() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        when(bookingRepository.findByItem_Owner_Id(userId, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfOwner(userId, State.ALL, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfOwnerWhenStateIsCurrent() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        when(bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(userId, now, now, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfOwner(userId, State.CURRENT, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfOwnerWhenStateIsPast() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        when(bookingRepository.findByItem_Owner_IdAndEndIsBefore(userId, now, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfOwner(userId, State.PAST, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfOwnerWhenStateIsFuture() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        when(bookingRepository.findByItem_Owner_IdAndStartIsAfter(userId, now, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfOwner(userId, State.FUTURE, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfOwnerWhenStateIsWaiting() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        when(bookingRepository.findByItem_Owner_IdAndStatus(userId, Status.WAITING, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfOwner(userId, State.WAITING, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getBookingsOfOwnerWhenStateIsRejected() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> mockPage = new PageImpl<>(new ArrayList<>());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        when(bookingRepository.findByItem_Owner_IdAndStatus(userId, Status.REJECTED, pageRequest)).thenReturn(mockPage);

        List<BookingResponseDto> result = bookingService.getBookingsOfOwner(userId, State.REJECTED, from, size);

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}