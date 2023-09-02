package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookerResponseDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.ItemResponseDto;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingResponseDtoTest {
    private JacksonTester<BookingResponseDto> json;
    private BookingResponseDto bookingResponseDto;

    public BookingResponseDtoTest(@Autowired JacksonTester<BookingResponseDto> json) {
        this.json = json;
    }

    @BeforeEach
    void beforeEach() {
        bookingResponseDto = new BookingResponseDto(
                1L,
                LocalDateTime.of(2030, 12, 25, 12, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00),
                Status.WAITING,
                new BookerResponseDto(1L),
                new ItemResponseDto(1L, "item"));
    }

    @Test
    void testJsonBookingDto() throws Exception {
        JsonContent<BookingResponseDto> result = json.write(bookingResponseDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2030-12-25T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2030-12-26T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }
}
