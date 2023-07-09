package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
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
    public ItemDto save(ItemDto itemDto, Long userId) {
        isItemRequestValid(userId, itemDto);
        return itemMapper.toItemDto(itemRepository.save(itemMapper.toItem(itemDto, userId), userId));
    }

    @Override
    public ItemDto update(Map<String, Object> updates, Long id, Long userId) {
        Item item = itemRepository.update(updates, id, userId);
        isItemRequestValid(userId, itemMapper.toItemDto(item));
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
        return itemRepository
                .search(text.toLowerCase())
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void isItemRequestValid(Long userId, ItemDto itemDto) {
        if (userRepository.getUserById(userId) == null) {
            throw new UserNotFoundException(String.format("User ID = %d not found.", userId));
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException(String.format("Item ID = %d invalid parameter %b"
                    , itemDto.getId()
                    , itemDto.getAvailable()));
        }
    }
}
