package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
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
    public BookingResponseDto create(@Valid @RequestBody BookingRequestDto bookingRequestDto,
                                     @RequestHeader(USER_HEADER) Long bookerId) {
        log.info("POST request to create Booking={}", bookingRequestDto);
        return bookingService.create(bookingRequestDto, bookerId);
    }

    @ResponseBody
    @PatchMapping("/{bookingId}")
    public BookingResponseDto update(@PathVariable("bookingId") Long bookingId,
                                     @RequestHeader(USER_HEADER) Long userId,
                                     @RequestParam Boolean approved) {
        log.info(
                "PATCH request to update status of Booking with ID={} to status={}",
                bookingId,
                approved
        );
        return bookingService.update(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId,
                                             @RequestHeader(USER_HEADER) Long userId) {
        log.info(
                "GET request to booking with id={} by user/owner with id={}",
                bookingId,
                userId
        );
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookingsOfUser(@RequestParam(name = "state", required = false, defaultValue = "ALL") String state,
                                                      @RequestHeader(USER_HEADER) Long bookerId) {
        log.info(
                "Received request to GET all bookings of user with id={} in state={}",
                bookerId,
                state
        );
        return bookingService.getBookingsOfUser(state, bookerId);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsOfOwner(@RequestParam(name = "state", required = false, defaultValue = "ALL") String state,
                                                       @RequestHeader(USER_HEADER) Long ownerId) {
        log.info(
                "GET request all bookings of owner with id={}, in state={}",
                ownerId,
                state
        );
        return bookingService.getBookingsOfOwner(state, ownerId);
    }
}