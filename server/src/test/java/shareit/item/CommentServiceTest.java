package shareit.item;

import org.assertj.core.api.Java6Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentMapper commentMapper;

    private Long userId;
    private Long itemId;
    private Long bookerId;
    private Long requestId;
    private Long commentId;
    private Long bookingId;
    private User user;
    private User booker;
    private Item item;
    private ItemDto itemDto;
    private Booking booking;
    private LocalDateTime current;
    private ItemRequest itemRequest;
    private Comment comment;
    private CommentRequest commentRequest;
    private CommentResponse commentResponse;
    private List<Booking> bookings;

    @BeforeEach
    void setUp() {
        bookerId = 1L;
        itemId = 2L;
        userId = 3L;
        requestId = 4L;
        bookingId = 5L;
        commentId = 6L;
        itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("request description")
                .requester(user)
                .created(LocalDateTime.now())
                .build();
        current = LocalDateTime.now();
        user = new User(userId, "user", "user@mail.ru");
        booker = new User(bookerId, "booker", "booker@mail.ru");
        bookings = new ArrayList<>();
        booking = new Booking(bookingId, current.minusHours(2), current.minusHours(1), item, user, Status.APPROVED);
        item = new Item(itemId, user, "name", "description", true, itemRequest, bookings);
        itemDto = new ItemDto(itemId, user.getId(), "name", "description", true, itemRequest.getId(), null, null, null);
        comment = new Comment(commentId, "comment", item, user, LocalDateTime.now());
        commentRequest = new CommentRequest("comment", userId, itemId);
        commentResponse = new CommentResponse(commentId, "comment", user.getName(), current);
    }

    @Test
    void getItemIdToCommentsWhenNoCommentsReturnsEmptyMap() {
        when(commentRepository.findAllByItem_IdIn(Set.of(1L)))
                .thenReturn(Collections.emptyList());

        Map<Long, List<CommentResponse>> itemIdToComments = commentService.getItemIdToComments(Set.of(1L));

        assertThat(itemIdToComments).isEmpty();
    }

    @Test
    void getCommentsOfItemReturnsListOfComments() {

        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(1L, "Comment 1", item, user, LocalDateTime.now()));
        comments.add(new Comment(2L, "Comment 2", item, user, LocalDateTime.now()));

        List<CommentResponse> expectedResponses = comments.stream()
                .map(comment -> new CommentResponse(comment.getId(), comment.getText(), user.getName(), comment.getCreated()))
                .collect(Collectors.toList());

        when(commentRepository.findAllByItem_Id(itemId)).thenReturn(comments);
        when(commentMapper.mapToDto(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            return new CommentResponse(comment.getId(), comment.getText(), user.getName(), comment.getCreated());
        });

        List<CommentResponse> actualResponses = commentService.getCommentsOfItem(itemId);

        Java6Assertions.assertThat(actualResponses).isEqualTo(expectedResponses);
    }

    @Test
    void getItemIdToCommentsReturnsMapOfItemIdToComments() {
        Set<Long> itemIds = new HashSet<>(Arrays.asList(1L, 2L));

        Item item1 = new Item(1L, user, "Item 1", "Description 1", true, itemRequest, new ArrayList<>());
        Item item2 = new Item(2L, user, "Item 2", "Description 2", true, itemRequest, new ArrayList<>());

        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(1L, "Comment 1 for Item 1", item1, user, LocalDateTime.now()));
        comments.add(new Comment(2L, "Comment 2 for Item 1", item1, user, LocalDateTime.now()));
        comments.add(new Comment(3L, "Comment 1 for Item 2", item2, user, LocalDateTime.now()));

        Map<Long, List<Comment>> expectedCommentsMap = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        Map<Long, List<CommentResponse>> expectedResponsesMap = expectedCommentsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(comment -> new CommentResponse(comment.getId(), comment.getText(), user.getName(), comment.getCreated()))
                                .collect(Collectors.toList())
                ));

        when(commentRepository.findAllByItem_IdIn(itemIds)).thenReturn(comments);
        when(commentMapper.mapToDto(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            return new CommentResponse(comment.getId(), comment.getText(), user.getName(), comment.getCreated());
        });

        Map<Long, List<CommentResponse>> actualResponsesMap = commentService.getItemIdToComments(itemIds);

        assertThat(actualResponsesMap).isEqualTo(expectedResponsesMap);
    }

    @Test
    void saveCommentAndThrowIfUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.saveComment(commentRequest, userId, itemId));

        assertEquals("User with ID=%d not found.",
                exception.getMessage());

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void saveCommentAndThrowIfItemNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findItemByIdWithBookingsFetched(anyLong())).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.saveComment(commentRequest, userId, itemId));

        assertEquals("Item with ID=%d not found.",
                exception.getMessage());
    }

    @Test
    void saveCommentSavesCommentSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findItemByIdWithBookingsFetched(anyLong())).thenReturn(Optional.of(item));

        bookings.add(booking);

        when(commentMapper.mapToComment(commentRequest, user, item)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.mapToDto(comment)).thenReturn(commentResponse);

        CommentResponse actual = commentService.saveComment(commentRequest, user.getId(), item.getId());

        assertThat(actual).isEqualTo(commentResponse);
    }

    @Test
    void saveCommentAndTrowIfUserNeverBooked() {
        Booking newBooking = new Booking(bookingId, current.minusHours(2), current.minusHours(1), item, booker, Status.APPROVED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findItemByIdWithBookingsFetched(anyLong())).thenReturn(Optional.of(item));

        bookings.add(newBooking);

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> commentService.saveComment(commentRequest, userId, itemId));

        assertEquals("User with ID=%d cannot post comment to item with ID=%d because the user never booked the item.",
                exception.getMessage());
    }

    @Test
    void saveCommentAndTrowIfStatusIsNotApproved() {
        Booking newBooking = new Booking(bookingId, current.minusHours(2), current.minusHours(1), item, user, Status.REJECTED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findItemByIdWithBookingsFetched(anyLong())).thenReturn(Optional.of(item));

        bookings.add(newBooking);

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> commentService.saveComment(commentRequest, userId, itemId));

        assertEquals("User with ID=%d cannot post comment to item with ID=%d because the user never booked the item.",
                exception.getMessage());
    }

    @Test
    void saveCommentAndTrowIfEndIsAfterCurrent() {
        Booking newBooking = new Booking(bookingId, current, current.plusHours(1), item, user, Status.APPROVED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findItemByIdWithBookingsFetched(anyLong())).thenReturn(Optional.of(item));

        bookings.add(newBooking);

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> commentService.saveComment(commentRequest, userId, itemId));

        assertEquals("User with ID=%d cannot post comment to item with ID=%d because the user never booked the item.",
                exception.getMessage());
    }
}
