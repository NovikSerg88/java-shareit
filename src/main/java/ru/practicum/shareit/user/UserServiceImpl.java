package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers() {
        return userRepository.getUsers().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toUserDto(userRepository.getUserById(id));
    }

    @Override
    public UserDto save(UserDto userDto) {
        isUserValid(userMapper.toUser(userDto));
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(Map<String, Object> updates, Long id) {
        User user = userRepository.update(updates, id);
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto delete(Long id) {
        return userMapper.toUserDto(userRepository.delete(id));
    }

    private void isUserValid(User user) {
        if (userRepository.getUsers().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new UserAlreadyExistsException(String.format("User with email %s already exists", user.getEmail()));
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException(String.format("Email %s is not valid", user.getEmail()));
        }
    }
}
