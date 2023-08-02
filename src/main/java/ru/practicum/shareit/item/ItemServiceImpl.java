package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import ru.practicum.shareit.booking.model.Booking;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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

    @Transactional(readOnly = true)
    @Override
    public ItemDto findById(Long itemId, Long userId) {
        Item item = findItemBookingsFetchedOrThrow(itemId);
        ItemDto dto = itemMapper.mapToDto(item);
        setBookings(dto, item, userId);
        addCommentsToDtoFromDb(dto, itemId);
        return dto;
    }

    @Override
    public ItemDto update(ItemDto dto, Long ownerId, Long itemId) {
        Item item = findItemByIdOrThrow(itemId);
        checkUserExists(ownerId);
        checkItemBelongsToUser(item, ownerId);
        updateItem(dto, item);
        itemRepository.save(item);
        return itemMapper.mapToDto(item);
    }

    @Override
    public ItemDto saveItem(ItemDto dto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User not found."));
        Item item = itemMapper.mapToItem(dto, owner);
        Item saved = itemRepository.save(item);
        return itemMapper.mapToDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getItemsForUser(long userId) {
        Map<Long, Item> idToItem = getItemsMapFetchedWithBookings(userId);
        Map<Long, List<CommentResponse>> itemIdToComments =
                commentService.getItemIdToComments(idToItem.keySet());
        return createItemDtos(idToItem, itemIdToComments);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchAvailableItems(String query) {
        if (ObjectUtils.isEmpty(query)) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.searchAllItemFetchOwnerByQuery(query);

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

    private void updateItem(ItemDto patchDto, Item toUpdate) {
        updateAvailable(patchDto, toUpdate);
        updateDescription(patchDto, toUpdate);
        updateName(patchDto, toUpdate);
    }

    private void updateName(ItemDto patchDto, Item toUpdate) {
        if (patchDto.getName() != null) {
            toUpdate.setName(patchDto.getName());
        }
    }

    private void updateDescription(ItemDto patchDto, Item toUpdate) {
        if (patchDto.getDescription() != null) {
            toUpdate.setDescription(patchDto.getDescription());
        }
    }

    private void updateAvailable(ItemDto patchDto, Item toUpdate) {
        if (patchDto.getAvailable() != null) {
            toUpdate.setAvailable(patchDto.getAvailable());
        }
    }

    private Item findItemBookingsFetchedOrThrow(Long itemId) {
        return itemRepository.findItemByIdWithBookingsFetched(itemId)
                .orElseThrow(() ->
                        new NotFoundException("Item not found."));
    }

    private Item findItemByIdOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found."));
    }

    private void checkItemBelongsToUser(Item item, Long ownerId) {
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Cannot update Item because it doesn't belong to user");
        }
    }

    private void checkUserExists(Long ownerId) {
        if (userRepository.getReferenceById(ownerId).getId() == null) {
            throw new NotFoundException("User not found.");
        }
    }

    private Consumer<Booking> addLastBookings(ItemDto dto) {
        return b -> {
            dto.setLastBooking(BookingResponseDto.builder()
                    .id(b.getId())
                    .bookerId(b.getBooker().getId())
                    .build());
        };
    }

    private Predicate<Booking> isStartedAndApprovedBeforeNow(LocalDateTime now) {
        return b -> (b.getEnd().isBefore(now) || (b.getEnd().isAfter(now) && b.getStart().isBefore(now)))
                && b.getStatus().equals(APPROVED);
    }

    private Consumer<Booking> addNextBooking(ItemDto dto) {
        return b -> {
            dto.setNextBooking(BookingResponseDto.builder()
                    .id(b.getId())
                    .bookerId(b.getBooker().getId())
                    .build());
        };
    }

    private Predicate<Booking> isActiveAfterNow(LocalDateTime now) {
        return b -> b.getStart().isAfter(now)
                && (b.getStatus().equals(WAITING)
                || b.getStatus().equals(APPROVED));
    }

    private List<ItemDto> createItemDtos(Map<Long, Item> idToItem,
                                         Map<Long, List<CommentResponse>> itemIdToComments) {
        return idToItem.values().stream()
                .map(i -> {
                    ItemDto dto = itemMapper.mapToDto(i);
                    setBookings(dto, i, i.getOwner().getId());
                    addCommentsToDtoFromMem(dto, itemIdToComments);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void setBookings(ItemDto dto, Item item, Long userId) {
        if (userId.equals(dto.getOwnerId())) {
            LocalDateTime now = LocalDateTime.now();
            List<Booking> bookings = item.getBookings();
            bookings.stream()
                    .filter(isActiveAfterNow(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .ifPresent(addNextBooking(dto));

            bookings.stream()
                    .filter(isStartedAndApprovedBeforeNow(now))
                    .max(Comparator.comparing(Booking::getStart))
                    .ifPresent(addLastBookings(dto));
        }
    }

    private Map<Long, Item> getItemsMapFetchedWithBookings(long userId) {
        return itemRepository.findAllByOwnerIdFetchBookings(userId)
                .stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
    }

    private void addCommentsToDtoFromMem(ItemDto dto,
                                         Map<Long, List<CommentResponse>> itemIdToComments) {
        if (itemIdToComments.containsKey(dto.getId())) {
            dto.setComments(itemIdToComments.get(dto.getId()));
        }
    }

    private void addCommentsToDtoFromDb(ItemDto dto, Long itemId) {
        List<CommentResponse> comments = commentService.getCommentsOfItem(itemId);
        dto.setComments(comments);
    }
}