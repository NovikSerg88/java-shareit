package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers();

    UserDto getUserById(Long userId);

    UserDto save(UserDto userDto);

    UserDto update(UserDto dto, Long userId);

    void delete(Long userId);
}
