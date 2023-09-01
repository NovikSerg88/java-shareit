package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class BookItemRequestDto {
    private final long itemId;
    @NotNull
    @FutureOrPresent
    private final LocalDateTime start;
    @NotNull
    @Future
    private final LocalDateTime end;

    @AssertTrue
    private boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }
}
