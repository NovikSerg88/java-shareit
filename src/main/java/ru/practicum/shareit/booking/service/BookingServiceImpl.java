package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.Status.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingResponseDto create(BookingRequestDto bookingRequestDto, Long bookerId) {
        Booking booking = bookingMapper.mapToBooking(bookingRequestDto, bookerId);
        if (booking.getItem().getAvailable().equals(false)) {
            throw new ValidationException("Item is not available");
        }
        if (booking.getBooker().getId().equals(booking.getItem().getOwner().getId())) {
            throw new NotFoundException("Owner cant book this item");
        }
        return bookingMapper.mapToBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto update(Long bookingId, Long ownerId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with ID=" + bookingId + " not found"));
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Status of booking cannot be updated because " +
                    "user is not the owner of item.");
        }
        if (approved) {
            if (booking.getStatus() == APPROVED) {
                throw new ValidationException("Cannot approve already approved Booking");
            }
            booking.setStatus(APPROVED);
        } else {
            if (booking.getStatus() == REJECTED) {
                throw new ValidationException("Cannot reject already rejected Booking");
            }
            booking.setStatus(REJECTED);
        }
        return bookingMapper.mapToBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with ID=" + bookingId + " not found"));
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();
        if (userId.equals(bookerId) || userId.equals(ownerId)) {
            return bookingMapper.mapToBookingResponseDto(booking);
        } else {
            throw new NotFoundException("Only owner or booker of a Booking can request data about it");
        }
    }

    @Override
    public List<BookingResponseDto> getBookingsOfUser(String stringState, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        State state = getState(stringState);
        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerId(userId, sortByStartDesc);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, sortByStartDesc);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndIsBefore(userId, now, sortByStartDesc);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartIsAfter(userId, now, sortByStartDesc);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING, sortByStartDesc);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED, sortByStartDesc);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        return bookings.stream()
                .map(bookingMapper::mapToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getBookingsOfOwner(String stringState, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        State state = getState(stringState);
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItem_Owner_Id(userId, sortByStartDesc);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(userId, now,
                        now, sortByStartDesc);
                break;
            case PAST:
                bookings = bookingRepository.findByItem_Owner_IdAndEndIsBefore(userId, now, sortByStartDesc);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItem_Owner_IdAndStartIsAfter(userId, now,
                        sortByStartDesc);
                break;
            case WAITING:
                bookings = bookingRepository.findByItem_Owner_IdAndStatus(userId, Status.WAITING, sortByStartDesc);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItem_Owner_IdAndStatus(userId, Status.REJECTED, sortByStartDesc);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        return bookings.stream()
                .map(bookingMapper::mapToBookingResponseDto)
                .collect(Collectors.toList());
    }

    private static State getState(String stringState) {
        State state;
        if (stringState == null) {
            state = State.ALL;
        } else {
            try {
                state = State.valueOf(stringState);
            } catch (Exception e) {
                throw new ValidationException(String.format("Unknown state: %s", stringState));
            }
        }
        return state;
    }
}