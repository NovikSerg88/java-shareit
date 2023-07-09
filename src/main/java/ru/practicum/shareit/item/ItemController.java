package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/items")
public class ItemController {
    private static final String SHARER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> save(@Valid @RequestBody ItemDto itemDto,
                                        @RequestHeader(SHARER) Long userId) {
        log.info("POST request /items with owner ID = {}", userId);
        return new ResponseEntity<>(itemService.save(itemDto, userId), HttpStatus.OK);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestBody Map<String, Object> updates,
                                          @PathVariable("itemId") Long id,
                                          @RequestHeader(SHARER) Long userId) {
        log.info("PATCH request /items/itemId = {} with owner ID = {}", id, userId);
        return new ResponseEntity<>(itemService.update(updates, id, userId), HttpStatus.OK);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable("itemId") Long id) {
        log.info("GET request /items/itemId = {}", id);
        return itemService.getById(id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(required = false) String text) {
        log.info("GET request /items/search item with text = {}", text);
        return itemService.search(text);
    }

    @GetMapping
    public List<ItemDto> getByUser(@RequestHeader(SHARER) Long userId) {
        log.info("GET request /items by user ID = {}", userId);
        return itemService.getByUser(userId);
    }
}
