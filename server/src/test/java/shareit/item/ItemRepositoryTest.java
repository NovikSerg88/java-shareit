package shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRepositoryTest {

    @Autowired
    private final ItemRepository itemRepository;
    private User user;
    private ItemRequest itemRequest;
    private Item item;
    private Booking booking;
    private LocalDateTime current;
    private List<Booking> bookings;

    @BeforeEach
    void setUp() {
        bookings = new ArrayList<>();
        current = LocalDateTime.now();
        user = new User(1L, "owner", "owner@user.ru");
        itemRequest = new ItemRequest(1L, "description 1", user, LocalDateTime.now());
        item = new Item(1L, user, "item", "description", true, itemRequest, bookings);
        booking = new Booking(1L, current, current.plusHours(1), item, user, Status.WAITING);
    }

    @Test
    void searchAllItemFetchOwnerByQuery() {
        PageRequest pageRequest = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "name"));
        List<Item> items = itemRepository.searchAllItemFetchOwnerByQuery("item", pageRequest);
        Assertions.assertNotNull(items);
    }

    @Test
    void findAllByOwnerIdFetchBookings() {
        PageRequest pageRequest = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "name"));
        List<Item> items = itemRepository.findAllByOwnerIdFetchBookings(user.getId(), pageRequest);
        Assertions.assertNotNull(items);
    }

    @Test
    void findItemByIdWithBookingsFetched() {
        Optional<Item> item = itemRepository.findItemByIdWithBookingsFetched(user.getId());
        Assertions.assertNotNull(item);
    }
}
