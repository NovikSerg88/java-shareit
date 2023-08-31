package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getUsers() {
        log.info("Received GET request to get all users");
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable("userId") Long userId) {
        log.info("Received GET request to get user with ID = {}", userId);
        return userService.getUserById(userId);
    }

    @PostMapping
    public UserDto save(@RequestBody UserDto userDto) {
        log.info("Received POST request to save user = {}", userDto);
        return userService.save(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody Map<String, Object> updates, @PathVariable("userId") Long userId) {
        log.info("Received PATCH request to update user with ID = {}", userId);
        return userService.update(updates, userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable("userId") Long userId) {
        log.info("Received DELETE request to delete user with ID = {}", userId);
        userService.delete(userId);
    }
}
