package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.*;

@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@RestController
public class RequestController {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader(value = USER_HEADER) @NotNull Long userId,
                                             @RequestBody @Valid ItemRequestDto dto) {
        log.info("Received POST request to create ItemRequest {}", dto);
        return requestClient.addRequest(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestsOfUser(@RequestHeader(value = USER_HEADER) @NotNull Long userId) {
        log.info("Received request to GET all ItemRequests of user with id={}", userId);
        return requestClient.getRequestsOfUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(value = USER_HEADER) @NotNull Long userId,
                                                 @RequestParam(value = "from", defaultValue = "0", required = false)
                                                 @PositiveOrZero int from,
                                                 @RequestParam(value = "size", defaultValue = "10", required = false)
                                                 @Positive int size) {
        log.info("Received request to GET all ItemRequests not belonging to user with id={}", userId);
        return requestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader(value = USER_HEADER) @NotNull Long userId,
                                             @PathVariable("requestId") Long id) {
        log.info("Received request to GET ItemRequest with id={}", id);
        return requestClient.getRequest(userId, id);
    }
}
