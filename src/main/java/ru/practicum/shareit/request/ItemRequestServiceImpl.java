package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestResponseDto create(ItemRequestDto dto, Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ItemRequest itemRequest = requestMapper.toItemRequest(dto, requester);
        return requestMapper.toResponseDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestResponseDto> getAllUserRequests(Long userId) {
        isUserExists(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterId(userId);
        List<ItemRequestResponseDto> responseDtos = new ArrayList<>();
        for (ItemRequest itemRequest : requests) {
            List<Item> items = itemRepository.findAllByRequestId(itemRequest.getId());
            List<ItemResponseDto> itemResponseDtos = items.stream()
                    .map(item -> requestMapper.mapToItemResponse(item, itemRequest.getId()))
                    .collect(Collectors.toList());

            ItemRequestResponseDto requestResponseDto = requestMapper.toResponseDto(itemRequest);
            requestResponseDto.setItems(itemResponseDtos);
            responseDtos.add(requestResponseDto);
        }
        return responseDtos;
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"));
        Page<ItemRequest> page = itemRequestRepository.findAllByRequesterIdNot(userId, pageRequest);
        List<ItemRequestResponseDto> responseDtos = new ArrayList<>();
        for (ItemRequest itemRequest : page) {
            List<Item> items = itemRepository.findAllByRequestId(itemRequest.getId());
            List<ItemResponseDto> itemResponseDtos = items.stream()
                    .map(item -> requestMapper.mapToItemResponse(item, itemRequest.getId()))
                    .collect(Collectors.toList());

            ItemRequestResponseDto requestResponseDto = requestMapper.toResponseDto(itemRequest);
            requestResponseDto.setItems(itemResponseDtos);
            responseDtos.add(requestResponseDto);
        }
        return responseDtos;

    }

    @Override
    public ItemRequestResponseDto getRequestById(Long requestId, Long userId) {
        isUserExists(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("ItemRequest not found"));
        List<Item> items = itemRepository.findAllByRequestId(requestId);
        List<ItemResponseDto> itemResponseDtos = items.stream()
                .map(item -> requestMapper.mapToItemResponse(item, itemRequest.getId()))
                .collect(Collectors.toList());
        ItemRequestResponseDto itemRequestResponseDto = requestMapper.toResponseDto(itemRequest);
        itemRequestResponseDto.setItems(itemResponseDtos);
        return itemRequestResponseDto;
    }

    private void isUserExists(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
