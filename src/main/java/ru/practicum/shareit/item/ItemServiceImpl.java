package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import ru.practicum.shareit.booking.model.Booking;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.Status.APPROVED;
import static ru.practicum.shareit.booking.model.Status.WAITING;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;
    private final CommentService commentService;
    private final ItemRequestRepository requestRepository;

    @Transactional(readOnly = true)
    @Override
    public ItemDto findById(Long itemId, Long userId) {
        Item item = itemRepository.findItemByIdWithBookingsFetched(itemId)
                .orElseThrow(() ->
                        new NotFoundException("Item not found."));
        ItemDto dto = itemMapper.mapToDto(item);
        setBookings(dto, item, userId);
        List<CommentResponse> comments = commentService.getCommentsOfItem(itemId);
        dto.setComments(comments);
        return dto;
    }

    @Override
    public ItemDto update(ItemDto dto, Long ownerId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found."));
        if (userRepository.getReferenceById(ownerId).getId() == null) {
            throw new NotFoundException("User not found.");
        }
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Cannot update Item because it doesn't belong to user");
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
        if (dto.getName() != null) {
            item.setName(dto.getName());
        }
        itemRepository.save(item);
        return itemMapper.mapToDto(item);
    }

    @Override
    public ItemDto saveItem(ItemDto dto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User not found."));
        ItemRequest request = null;
        if (dto.getRequestId() != null) {
            request = requestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request not found"));
        }
        Item item = itemMapper.mapToItem(dto, owner, request);
        return itemMapper.mapToDto(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getItemsForUser(long userId, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "name"));
        Map<Long, Item> idToItem = itemRepository.findAllByOwnerIdFetchBookings(userId, pageRequest)
                .stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        Map<Long, List<CommentResponse>> itemIdToComments =
                commentService.getItemIdToComments(idToItem.keySet());
        return idToItem.values().stream()
                .map(i -> {
                    ItemDto dto = itemMapper.mapToDto(i);
                    setBookings(dto, i, i.getOwner().getId());
                    if (itemIdToComments.containsKey(dto.getId())) {
                        dto.setComments(itemIdToComments.get(dto.getId()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchAvailableItems(String query, int from, int size) {
        if (ObjectUtils.isEmpty(query)) {
            return Collections.emptyList();
        }
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "name"));
        List<Item> items = itemRepository.searchAllItemFetchOwnerByQuery(query, pageRequest);

        return items
                .stream()
                .map(itemMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItemsForUser(long userId) {
        itemRepository.deleteAllByOwner_Id(userId);
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        itemRepository.deleteItemByIdAndOwner_Id(userId, itemId);
    }

    private void setBookings(ItemDto dto, Item item, Long userId) {
        if (userId.equals(dto.getOwnerId())) {
            LocalDateTime now = LocalDateTime.now();
            List<Booking> bookings = item.getBookings();
            bookings.stream()
                    .filter(b -> b.getStart().isAfter(now)
                            && (b.getStatus().equals(WAITING)
                            || b.getStatus().equals(APPROVED)))
                    .min(Comparator.comparing(Booking::getStart))
                    .ifPresent(b -> dto.setNextBooking(BookingResponseDto.builder()
                            .id(b.getId())
                            .bookerId(b.getBooker().getId())
                            .build()));

            bookings.stream()
                    .filter(b -> (b.getEnd().isBefore(now) || (b.getEnd().isAfter(now) && b.getStart().isBefore(now)))
                            && b.getStatus().equals(APPROVED))
                    .max(Comparator.comparing(Booking::getStart))
                    .ifPresent(b -> dto.setLastBooking(BookingResponseDto.builder()
                            .id(b.getId())
                            .bookerId(b.getBooker().getId())
                            .build()));
        }
    }
}