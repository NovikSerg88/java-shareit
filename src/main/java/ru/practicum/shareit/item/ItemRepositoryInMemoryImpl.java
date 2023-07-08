package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ItemIdException;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.UserIdException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemRepositoryInMemoryImpl implements ItemRepository {

    private Map<Long, Item> items = new HashMap<>();
    private Long initialId = 0L;

    @Override
    public Item save(Long userId, Item item) {
        item.setId(++initialId);
        item.setOwnerId(userId);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Map<String, Object> updates, Long id, Long userId) {
        Item item = items.get(id);
        if (!item.getOwnerId().equals(userId)) {
            throw new UserIdException("access denied by another user.");
        }
        if (updates.containsKey("name")) {
            String newName = (String) updates.get("name");
            item.setName(newName);
        }
        if (updates.containsKey("description")) {
            String description = (String) updates.get("description");
            item.setDescription(description);
        }
        if (updates.containsKey("available")) {
            Boolean available = (Boolean) updates.get("available");
            item.setAvailable(available);
        }
        return item;
    }

    @Override
    public Item getById(Long id) {
        if (!items.containsKey(id)) {
            throw new ItemIdException(String.format("item id %d not found", id));
        }
        return items.get(id);
    }

    @Override
    public List<Item> getByUser(Long id) {
        return items.values()
                .stream()
                .filter(item -> item.getOwnerId().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        List<Item> searchItem = new ArrayList<>();
        if (!text.isBlank()) {
            searchItem = items.values()
                    .stream()
                    .filter(Item::getAvailable)
                    .filter(item -> item.getName().toLowerCase().contains(text)
                            || item.getDescription().toLowerCase().contains(text))
                    .collect(Collectors.toList());
        }
        return searchItem;
    }
}
