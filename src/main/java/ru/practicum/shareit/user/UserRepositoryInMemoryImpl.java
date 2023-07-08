package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.UserIdException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserRepositoryInMemoryImpl implements UserRepository {

    private Map<Long, User> users = new HashMap<>();
    private Long initialId = 0L;

    @Override
    public List<User> getUsers() {
        log.info("list of users request");
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new UserIdException(String.format("User %s not found", id));
        }
        log.info("request user by id {}", id);
        return users.get(id);
    }

    @Override
    public User save(User user) {
        isValid(user);
        user.setId(++initialId);
        users.put(user.getId(), user);
        log.info("create user {} request", user);
        return user;

    }

    @Override
    public User update(Map<String, Object> updates, Long id) {
        User user = users.get(id);
        if (user != null) {
            if (updates.containsKey("name")) {
                String newName = (String) updates.get("name");
                user.setName(newName);
            }
            if (updates.containsKey("email")) {
                String newEmail = (String) updates.get("email");
                if(users.values().stream().anyMatch(u -> u.getEmail().equals(newEmail) && u.getId() != user.getId())) {
                    throw new UserAlreadyExistsException("already exists");
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

    private boolean isValid(User user) {
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new UserAlreadyExistsException(String.format("User %s already exists", user.getEmail()));
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException(String.format("email %s is invalid", user.getEmail()));
        }
        return true;
    }
}
