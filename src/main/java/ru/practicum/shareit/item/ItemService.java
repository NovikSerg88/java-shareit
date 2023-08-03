package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto findById(Long itemId, Long userId);

    ItemDto update(ItemDto dto, Long ownerId, Long itemId);

    ItemDto saveItem(ItemDto dto, Long userId);

    List<ItemDto> getItemsForUser(long userId);

    List<ItemDto> searchAvailableItems(String query);

    void deleteItemsForUser(long userId);

    void deleteItem(long userId, long itemId);
}