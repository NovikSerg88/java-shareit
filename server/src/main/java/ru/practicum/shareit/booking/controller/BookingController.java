package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @ResponseBody
    @PostMapping
    public BookingResponseDto create(@RequestBody BookingRequestDto bookingRequestDto,
                                     @RequestHeader(USER_HEADER) Long bookerId) {
        log.info("Received POST request to create Booking={}", bookingRequestDto);
        return bookingService.create(bookingRequestDto, bookerId);
    }

    @ResponseBody
    @PatchMapping("/{bookingId}")
    public BookingResponseDto update(@PathVariable("bookingId") Long bookingId,
                                     @RequestHeader(USER_HEADER) Long userId,
                                     @RequestParam Boolean approved) {
        log.info("Received PATCH request to update status of Booking with ID={} to status={}", bookingId, approved);
        return bookingService.update(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId,
                                             @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received GET request to to get booking with id={} by user/owner with id={}", bookingId, userId);
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookingsOfUser(@RequestHeader(USER_HEADER) Long bookerId,
                                                      @RequestParam(value = "state", required = false, defaultValue = "ALL") State state,
                                                      @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                                      @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("Received GET request to get all bookings of user with id={} in state={}", bookerId, state);
        return bookingService.getBookingsOfUser(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsOfOwner(@RequestHeader(USER_HEADER) Long ownerId,
                                                       @RequestParam(value = "state", required = false, defaultValue = "ALL") State state,
                                                       @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                                       @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("Received GET request to get all bookings of owner with id={}, in state={}", ownerId, state);
        return bookingService.getBookingsOfOwner(ownerId, state, from, size);
    }
}