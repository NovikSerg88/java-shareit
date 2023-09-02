package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestResponseDto create(ItemRequestDto dto, Long requesterId);

    List<ItemRequestResponseDto> getAllUserRequests(Long userId);

    List<ItemRequestResponseDto> getAllRequests(Long userId, int from, int size);

    ItemRequestResponseDto getRequestById(Long requestId, Long userId);
}
