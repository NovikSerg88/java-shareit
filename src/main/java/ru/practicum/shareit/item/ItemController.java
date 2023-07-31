package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final CommentService commentService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    @ResponseBody
    public ItemDto createItem(@RequestHeader(USER_HEADER) Long userId,
                                              @RequestBody @Valid ItemDto dto) {
        log.info(
                "Received POST request to create Item {} by user with id = {}",
                dto,
                userId
        );
        dto.setOwnerId(userId);
        return itemService.saveItem(dto);
    }

    @PatchMapping("/{itemId}")
    @ResponseBody
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(value = USER_HEADER) Long userId,
                                              @RequestBody ItemDto dto,
                                              @PathVariable("itemId") Long itemId) {
        log.info(
                "Received PATCH request to update Item {} by user with id = {}",
                dto,
                userId
        );
        dto.setId(itemId);
        return ResponseEntity.ok(itemService.update(dto, userId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable("itemId") Long itemId,
                                               @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received request to GET Item by id = {}", itemId);
        return ResponseEntity.ok(itemService.findById(itemId, userId));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItemsForUser(@RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Received request to GET items for user with id={}", userId);
        return ResponseEntity.ok(itemService.getItemsForUser(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam("text") String query) {
        log.info("Received GET request to search for items by query = {}", query);
        return ResponseEntity.ok(itemService.searchAvailableItems(query));
    }

    @PostMapping("/{itemId}/comment")
    @ResponseBody
    public ResponseEntity<CommentResponse> postComment(@PathVariable("itemId") Long itemId,
                                                       @RequestHeader(USER_HEADER) Long userId,
                                                       @RequestBody @Valid CommentRequest dto) {
        log.info(
                "Received POST request to create comment to item with ID={} by user with ID={}",
                itemId,
                userId
        );
        dto.setUserId(userId);
        dto.setItemId(itemId);
        return ResponseEntity.ok(commentService.saveComment(dto));
    }

}