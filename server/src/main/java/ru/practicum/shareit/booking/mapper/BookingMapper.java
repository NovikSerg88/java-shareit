package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookerResponseDto;
import ru.practicum.shareit.booking.dto.ItemResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    public BookingResponseDto mapToBookingResponseDto(Booking booking) {
        ItemResponseDto item = new ItemResponseDto(booking.getItem().getId(),
                booking.getItem().getName());
        BookerResponseDto booker = new BookerResponseDto(booking.getBooker().getId());

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(booker)
                .item(item)
                .build();
    }

    public Booking mapToBooking(BookingRequestDto dto, Item item, User booker) {
        return Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();
    }
}