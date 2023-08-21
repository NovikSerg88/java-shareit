package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(BookingRequestDto bookingRequestDto, Long bookerId);

    BookingResponseDto update(Long bookingId, Long userId, Boolean approved);

    BookingResponseDto getBookingById(Long bookingId, Long userId);

    List<BookingResponseDto> getBookingsOfUser(Long userId, String state, Integer from, Integer size);

    List<BookingResponseDto> getBookingsOfOwner(Long userId, String state, int from, int size);
}