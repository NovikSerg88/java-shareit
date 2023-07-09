package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;

import java.util.*;

@Component
public class UserRepositoryInMemoryImpl implements UserRepository {

    private Map<Long, User> users = new HashMap<>();
    private Long initialId = 0L;

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new UserNotFoundException(String.format("User %d not found", id));
        }
        return users.get(id);
    }

    @Override
    public User save(User user) {
        user.setId(++initialId);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(Map<String, Object> updates, Long id) {
        User user = users.get(id);
        if (user != null) {
            if (updates.containsKey(SearchBy.name.getColumnName())) {
                String newName = (String) updates.get(SearchBy.name.getColumnName());
                user.setName(newName);
            }
            if (updates.containsKey(SearchBy.email.getColumnName())) {
                String newEmail = (String) updates.get(SearchBy.email.getColumnName());
                if (users.values().stream().anyMatch(u -> u.getEmail().equals(newEmail) && !Objects.equals(u.getId(), user.getId()))) {
                    throw new UserAlreadyExistsException("User already exists");
                }
                user.setEmail(newEmail);
            }
        }
        return user;
    }

    @Override
    public User delete(Long id) {
        return users.remove(id);
    }
}
