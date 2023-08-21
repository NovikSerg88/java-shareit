package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        return userMapper.toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with ID = %d not found", userId))));
    }

    @Override
    public UserDto save(UserDto userDto) {
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(Map<String, Object> updates, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with ID = %d not found", userId)));
        List<UserDto> users = userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
        if (updates.containsKey("name")) {
            String newName = (String) updates.get("name");
            user.setName(newName);
        }
        if (updates.containsKey("email")) {
            String newEmail = (String) updates.get("email");
            if (users.stream().anyMatch(u -> u.getEmail().equals(newEmail) && !u.getId().equals(user.getId()))) {
                throw new UserAlreadyExistsException(String.format("User with ID = %d already exists", userId));
            }
            user.setEmail(newEmail);
        }
        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
