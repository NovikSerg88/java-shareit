package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookerResponseDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.ItemResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BookingMapperTest {
    private Long bookerId;
    private Long itemId;
    private Long userId;
    private Long bookingId;
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
    private final BookingMapper bookingMapper = new BookingMapper();

    @BeforeEach
    void setUp() {
        bookerId = 1L;
        itemId = 2L;
        userId = 3L;
        requestId = 4L;
        bookingId = 5L;
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
    public void testMapToBookingResponseDto() {

        BookingResponseDto responseDto = bookingMapper.mapToBookingResponseDto(booking);

        assertNotNull(responseDto);
        assertEquals(booking.getId(), responseDto.getId());
        assertEquals(booking.getStart(), responseDto.getStart());
        assertEquals(booking.getEnd(), responseDto.getEnd());
        Assertions.assertEquals(booking.getStatus(), responseDto.getStatus());
        assertEquals(booking.getItem().getId(), responseDto.getItem().getId());
        assertEquals(booking.getItem().getName(), responseDto.getItem().getName());
        assertEquals(booking.getBooker().getId(), responseDto.getBooker().getId());
    }

    @Test
    public void testMapToBooking() {

        Booking booking = bookingMapper.mapToBooking(bookingRequestDto, item, booker);

        assertNotNull(booking);
        assertEquals(bookingRequestDto.getStart(), booking.getStart());
        assertEquals(bookingRequestDto.getEnd(), booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(Status.WAITING, booking.getStatus());
    }
}