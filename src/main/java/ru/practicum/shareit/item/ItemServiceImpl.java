package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserIdException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;

    @Override
    public ItemDto save(Long userId, ItemDto itemDto) {
        isItemRequestValid(userId, itemDto);
        return itemMapper.toItemDto(itemRepository.save(userId, itemMapper.toItem(itemDto, userId)));
    }

    @Override
    public ItemDto update(Map<String, Object> updates, Long id, Long userId) {
        if (userId == null) {
            throw new UserIdException(String.format("user %d id not found.", userId));
        }
        Item item = itemRepository.update(updates, id, userId);
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getById(Long id) {
        return itemMapper.toItemDto(itemRepository.getById(id));
    }

    @Override
    public List<ItemDto> getByUser(Long id) {
        return itemRepository.getByUser(id)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        text = text.toLowerCase();
        return itemRepository
                .search(text)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void isItemRequestValid(Long userId, ItemDto itemDto) {
        if (userRepository.getUserById(userId) == null) {
            throw new UserIdException(String.format("user %d id not found.", userId));
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException(String.format("item request invalid parameter %s", itemDto.getAvailable()));
        }
    }
}
