package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.Status.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingResponseDto create(BookingRequestDto bookingRequestDto, Long bookerId) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));
        Booking booking = bookingMapper.mapToBooking(bookingRequestDto, item, booker);
        if (booking.getItem() == null || booking.getItem().getAvailable() == null || !booking.getItem().getAvailable()) {
            throw new ValidationException("Item is not available");
        }

        if (booking.getBooker() == null || booking.getBooker().getId().equals(booking.getItem().getOwner().getId())) {
            throw new NotFoundException("Owner can't book this item");
        }
        return bookingMapper.mapToBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto update(Long bookingId, Long ownerId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with ID=" + bookingId + " not found"));
        if (ownerId == null || !ownerId.equals(booking.getItem().getOwner().getId())) {
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
        if (userId != null && (userId.equals(bookerId) || userId.equals(ownerId))) {
            return bookingMapper.mapToBookingResponseDto(booking);
        } else {
            throw new NotFoundException("Only owner or booker of a Booking can request data about it");
        }
    }

    @Override
    public List<BookingResponseDto> getBookingsOfUser(Long userId, String stringState, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (from < 0) {
            throw new ValidationException("Page cant be less then null");
        }
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> bookings;
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        State state = getState(stringState);
        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerId(userId, pageRequest);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndIsBefore(userId, now, pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartIsAfter(userId, now, pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, WAITING, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, REJECTED, pageRequest);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        return bookings.stream()
                .map(bookingMapper::mapToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getBookingsOfOwner(Long userId, String stringState, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (from < 0) {
            throw new ValidationException("Page cant be less then null");
        }
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        Page<Booking> bookings;
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        State state = getState(stringState);
        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItem_Owner_Id(userId, pageRequest);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(userId, now,
                        now, pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.findByItem_Owner_IdAndEndIsBefore(userId, now, pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItem_Owner_IdAndStartIsAfter(userId, now,
                        pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.findByItem_Owner_IdAndStatus(userId, WAITING, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItem_Owner_IdAndStatus(userId, REJECTED, pageRequest);
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