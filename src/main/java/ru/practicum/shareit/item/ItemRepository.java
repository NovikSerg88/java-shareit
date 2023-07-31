package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

public interface ItemRepository {

    Item save(Item item, Long userId);

    Item update(Map<String, Object> updates, Long id, Long userId);

    Item getById(Long id);

    List<Item> getByUser(Long id);

    List<Item> search(String text);
}
