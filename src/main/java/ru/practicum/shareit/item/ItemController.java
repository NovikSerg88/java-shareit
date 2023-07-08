package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private static final String SHARER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> save(@Valid @RequestBody ItemDto itemDto,
                                        @RequestHeader(SHARER) Long userId
    ) {
        return new ResponseEntity<>(itemService.save(userId, itemDto), HttpStatus.OK);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestBody Map<String, Object> updates,
                                          @PathVariable("itemId") Long id,
                                          @RequestHeader(SHARER) Long userId) {
        return new ResponseEntity<>(itemService.update(updates, id, userId), HttpStatus.OK);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable("itemId") Long id) {
        return itemService.getById(id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(required = false) String text) {
        return itemService.search(text);
    }

    @GetMapping
    public List<ItemDto> getByUser(@RequestHeader(SHARER) Long userId) {
        return itemService.getByUser(userId);
    }
}
