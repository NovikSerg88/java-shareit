package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponseDto create(@RequestBody ItemRequestDto itemRequestDto,
                                         @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received POST request to create item request from user with ID = {}", userId);
        return itemRequestService.create(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getAllUserRequests(@RequestHeader(USER_HEADER) Long userId) {
        log.info("Received GET request to get all requests of user with ID = {}", userId);
        return itemRequestService.getAllUserRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllRequests(@RequestHeader(USER_HEADER) Long userId,
                                                       @RequestParam(value = "from", defaultValue = "0", required = false) Integer from,
                                                       @RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {
        log.info("Received GET request to get all requests of users");
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getRequestById(@RequestHeader(USER_HEADER) Long userId,
                                                 @PathVariable("requestId") Long requestId) {
        log.info("Received GET request to get ItemRequest with ID = {}", requestId);
        return itemRequestService.getRequestById(requestId, userId);
    }
}
