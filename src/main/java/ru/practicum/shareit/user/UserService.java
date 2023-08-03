package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

public interface UserService {

    List<UserDto> getUsers();

    UserDto getUserById(Long userId);

    UserDto save(UserDto userDto);

    UserDto update(Map<String, Object> updates, Long userId);

    void delete(Long userId);
}
