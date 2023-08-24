package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
@Setter
public final class BookingResponseDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Status status;
    private BookerResponseDto booker;
    private ItemResponseDto item;
}
