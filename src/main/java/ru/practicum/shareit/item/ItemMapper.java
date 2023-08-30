package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Component
@RequiredArgsConstructor
public class ItemMapper {

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

    public Item mapToItem(ItemDto itemDto, User owner, ItemRequest request) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .owner(owner)
                .available(itemDto.getAvailable())
                .request(request)
                .build();
    }
}