package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.Status.APPROVED;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CommentMapper mapper;

    @Override
    public CommentResponse saveComment(CommentRequest commentRequest, Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID=%d not found."));
        Item item = itemRepository.findItemByIdWithBookingsFetched(itemId)
                .orElseThrow(() -> new NotFoundException("Item with ID=%d not found."));
        boolean userHasBooked = item.getBookings().stream()
                .anyMatch(booking -> booking.getBooker().getId().equals(userId)
                        && booking.getStatus().equals(APPROVED)
                        && booking.getEnd().isBefore(LocalDateTime.now()));
        if (!userHasBooked) {
            throw new ValidationException("User with ID=%d cannot post comment to item with ID=%d " +
                    "because the user never booked the item.");
        }
        Comment saved = commentRepository.save(mapper.mapToComment(commentRequest, user, item));
        return mapper.mapToDto(saved);
    }

    @Override
    public List<CommentResponse> getCommentsOfItem(Long itemId) {
        return commentRepository.findAllByItem_Id(itemId)
                .stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<CommentResponse>> getItemIdToComments(Set<Long> itemIds) {
        return commentRepository.findAllByItem_IdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId(),
                        Collectors.mapping(mapper::mapToDto, Collectors.toList())));
    }
}