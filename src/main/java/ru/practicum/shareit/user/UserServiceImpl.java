package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        User user = userMapper.toUser(getUserById(userId));
        if (updates.containsKey(SearchBy.NAME.getColumnName())) {
            String newName = (String) updates.get(SearchBy.NAME.getColumnName());
            user.setName(newName);
        }
        if (updates.containsKey(SearchBy.EMAIL.getColumnName())) {
            String newEmail = (String) updates.get(SearchBy.EMAIL.getColumnName());
            if (getUsers().stream().anyMatch(u -> u.getEmail().equals(newEmail) && !Objects.equals(u.getId(), user.getId()))) {
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
