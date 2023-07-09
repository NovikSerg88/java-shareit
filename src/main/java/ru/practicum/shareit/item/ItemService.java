package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Map;

public interface ItemService {

    ItemDto save(ItemDto itemDto, Long userId);

    ItemDto update(Map<String, Object> updates, Long id, Long userId);

    ItemDto getById(Long id);

    List<ItemDto> getByUser(Long id);

    List<ItemDto> search(String text);
}
