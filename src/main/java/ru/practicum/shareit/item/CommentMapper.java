package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

import static ru.practicum.shareit.booking.model.Status.APPROVED;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public CommentResponse mapToDto(Comment domain) {
        return CommentResponse.builder()
                .id(domain.getId())
                .text(domain.getText())
                .authorName(domain.getAuthor().getName())
                .created(domain.getCreated())
                .build();
    }

    public Comment mapToComment(CommentRequest dto, Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID=%d not found."));
        Item item = itemRepository.findItemByIdWithBookingsFetched(itemId)
                .orElseThrow(() -> new NotFoundException("Item with ID=%d not found."));
        checkUserBookedItem(item, userId);
        return Comment.builder()
                .text(dto.getText())
                .item(item)
                .created(LocalDateTime.now())
                .author(user)
                .build();
    }

    private void checkUserBookedItem(Item item, Long userId) {
        Optional<Booking> booking = item.getBookings().stream()
                .filter(hasUserBookedAnItem(userId))
                .findFirst();
        if (booking.isEmpty()) {
            throw new ValidationException("User with ID=%d cannot post comment to item with ID=%d " +
                    "because the user never booked the item.");
        }
    }

    private Predicate<Booking> hasUserBookedAnItem(Long userId) {
        return b -> b.getBooker().getId().equals(userId)
                && b.getStatus().equals(APPROVED)
                && b.getEnd().isBefore(LocalDateTime.now());
    }
}