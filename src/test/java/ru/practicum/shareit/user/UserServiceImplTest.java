package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Spy
    @InjectMocks
    private UserServiceImpl userService;
    private UserDto userDto;
    private User user;
    private Map<String, Object> updates;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "user", "user@user.ru");
        user = new User(1L, "user", "user@user.ru");
        updates = new HashMap<>();
    }

    public static User fromDto(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static UserDto getUserDto() {
        return UserDto.builder()
                .email("user@user.ru")
                .name("user")
                .build();
    }

    public static User getMockUser(Long id) {
        return User.builder()
                .id(id)
                .name("user")
                .email("user@user.ru")
                .build();
    }

    public static UserDto fromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Test
    void updateWhenUserNotPresentThrows() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.update(updates, userDto.getId()));
        assertEquals("User with ID = 1 not found", exception.getMessage());
    }

    @Test
    void updateByNameAndReturn() {
        Long userId = 1L;
        String newName = "New Name";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);

        userService.update(updates, userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUserDto(user);
    }

    @Test
    void updateByEmailAndReturn() {
        Long userId = 1L;
        String newEmail = "newEmail@user.ru";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", newEmail);

        userService.update(updates, userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUserDto(user);
    }

    @Test
    void testUpdateDuplicateEmail() {
        Long userId = 1L;
        String existingEmail = "existing@user.ru";

        User existingUser = user;
        existingUser.setEmail(existingEmail);

        List<User> userList = List.of(existingUser);
        Map<String, Object> updates = new HashMap<>();
        updates.put("email", existingEmail);
        String newEmail = (String) updates.get("email");
        if (userList.stream().anyMatch(u -> u.getEmail().equals(newEmail) && !u.getId().equals(user.getId()))) {
            assertThrows(UserAlreadyExistsException.class, () -> userService.update(updates, userId));
        }
    }

    @Test
    void shouldExceptionWhenGetUserWithWrongId() {
        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(-1L));
        Assertions.assertEquals("User with ID = -1 not found", exception.getMessage());
    }

    @Test
    void shouldExceptionWhenCreateUserWithExistEmail() {
        UserDto dto = getUserDto();
        User unsaved = getMockUser(null);

        when(userMapper.toUser(dto)).thenReturn(unsaved);
        when(userRepository.save(unsaved)).thenThrow(ValidationException.class);

        assertThrows(ValidationException.class, () -> userService.save(dto));
    }

    @Test
    void createWhenEmailUniqueCreatesNewUser() {
        UserDto dto = getUserDto();
        User unsaved = getMockUser(null);
        User saved = getMockUser(1L);
        UserDto toBeReturned = getUserDto();
        toBeReturned.setId(1L);

        when(userMapper.toUser(dto)).thenReturn(unsaved);
        when(userRepository.save(unsaved)).thenReturn(saved);
        when(userMapper.toUserDto(saved)).thenReturn(toBeReturned);

        UserDto created = userService.save(dto);
        assertThat(created.getId()).isEqualTo(saved.getId());
        assertThat(created.getName()).isEqualTo(saved.getName());
        assertThat(created.getEmail()).isEqualTo(saved.getEmail());
    }

    @Test
    void getAllUsersWhenHasUsersReturnsAllOfThem() {
        List<User> users = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            users.add(getMockUser(i));
        }

        when(userRepository.findAll()).thenReturn(users);
        List<UserDto> dtos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            dtos.add(fromUser(users.get(i)));
        }
        for (int i = 0; i < 5; i++) {
            when(userMapper.toUserDto(users.get(i))).thenReturn(dtos.get(i));
        }

        List<UserDto> allUsers = userService.getUsers();
        assertThat(allUsers).isEqualTo(dtos);
    }

    @Test
    void getAllUsersWhenNoUsersReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<UserDto> allUsers = userService.getUsers();
        assertThat(allUsers.isEmpty());
    }

    @Test
    public void shouldDeleteUser() {
        Long userId = 1L;

        userService.delete(userId);

        verify(userRepository).deleteById(userId);
    }
}