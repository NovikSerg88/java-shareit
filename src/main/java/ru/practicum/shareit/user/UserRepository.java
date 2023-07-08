package ru.practicum.shareit.user;

import java.util.List;
import java.util.Map;

public interface UserRepository {

    List<User> getUsers();

    User getUserById(Long id);

    User save(User user);

    User update(Map<String, Object> updates, Long id);

    User delete(Long id);
}
