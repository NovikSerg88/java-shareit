package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;


@Component
@RequiredArgsConstructor
public class ItemMapper {

    private final UserRepository userRepository;


    public ItemDto mapToDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .ownerId(item.getOwner().getId())
                .name(item.getName())
                .description(item.getDescription())
                .id(item.getId())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }


    public Item mapToDomain(ItemDto itemDto) {
        User owner = userRepository.findById(itemDto.getOwnerId())
                .orElseThrow(() ->
                     new NotFoundException("User with ID=%d not found."));

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .owner(owner)
                .available(itemDto.getAvailable())
                .request(null) // TODO fix, after implementing ItemRequest logic
                .build();
    }
}