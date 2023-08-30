package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookerResponseDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.ItemResponseDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    BookingService bookingService;

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final BookingRequestDto bookingRequestDto = new BookingRequestDto(
            1L,
            LocalDateTime.of(2030, 12, 25, 12, 0, 0),
            LocalDateTime.of(2030, 12, 26, 12, 0, 0));
    private final BookingResponseDto bookingResponseDto = new BookingResponseDto(
            1L,
            LocalDateTime.of(2030, 12, 25, 12, 0, 0),
            LocalDateTime.of(2030, 12, 26, 12, 0, 0),
            Status.WAITING,
            new BookerResponseDto(1L),
            new ItemResponseDto(1L, "testItem")
    );
    private final List<Object> listBookingDto = new ArrayList<>();

    @Test
    void createBooking() throws Exception {
        when(bookingService.create(any(), any(Long.class)))
                .thenReturn(bookingResponseDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        is(bookingResponseDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end",
                        is(bookingResponseDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())));
    }

    @Test
    void getBooking() throws Exception {
        when(bookingService.getBookingById(any(Long.class), any(Long.class)))
                .thenReturn(bookingResponseDto);

        mvc.perform(get("/bookings/1")
                        .content(mapper.writeValueAsString(bookingResponseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        is(bookingResponseDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end",
                        is(bookingResponseDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString()), Status.class));
    }

    @Test
    void getBookings() throws Exception {
        when(bookingService.getBookingsOfUser(any(Long.class), any(String.class),
                any(Integer.class), nullable(Integer.class)))
                .thenReturn(List.of(bookingResponseDto));

        mvc.perform(get("/bookings")

                        .content(mapper.writeValueAsString(listBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(bookingResponseDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].end", is(bookingResponseDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].item.id", is(bookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.[0].booker.id", is(bookingResponseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(bookingResponseDto.getStatus().toString())));
    }

    @Test
    void getBookingsByOwner() throws Exception {
        when(bookingService.getBookingsOfOwner(any(Long.class), any(String.class),
                any(Integer.class), nullable(Integer.class)))
                .thenReturn(List.of(bookingResponseDto));

        mvc.perform(get("/bookings/owner?from=0&size=10")
                        .content(mapper.writeValueAsString(listBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(bookingResponseDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].end", is(bookingResponseDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].item.id", is(bookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.[0].booker.id", is(bookingResponseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(bookingResponseDto.getStatus().toString())));
    }

    @Test
    void updateBooking() throws Exception {
        when(bookingService.update(any(Long.class), any(Long.class), any(Boolean.class)))
                .thenReturn(bookingResponseDto);
        mvc.perform(patch("/bookings/1")
                        .content(mapper.writeValueAsString(bookingResponseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, 1)
                        .queryParam("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        is(bookingResponseDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end",
                        is(bookingResponseDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())));
    }

    @Test
    public void testIsEndAfterStartValidation() throws Exception {
        BookingRequestDto invalidRequest = new BookingRequestDto(
                1L,
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 24, 12, 0, 0));

        when(bookingService.create(any(), any(Long.class)))
                .thenReturn(bookingResponseDto);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(invalidRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, 1))
                .andExpect(status().is4xxClientError());
    }
}

