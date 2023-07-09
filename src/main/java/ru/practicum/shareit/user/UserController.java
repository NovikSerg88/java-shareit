package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

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
        log.info("GET request /users");
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable("userId") Long id) {
        log.info("GET request /users/userId = {}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public ResponseEntity<UserDto> save(@RequestBody UserDto userDto) {
        log.info("POST request /users with ID = {}", userDto.getId());
        return new ResponseEntity<>(userService.save(userDto), HttpStatus.OK);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(@RequestBody Map<String, Object> updates
            , @PathVariable("userId") Long id) {
        log.info("PATCH request /users/userID with ID = {}", id);
        return new ResponseEntity<>(userService.update(updates, id), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public UserDto delete(@PathVariable("userId") Long id) {
        log.info("DELETE request /users/userID with ID = {}", id);
        return userService.delete(id);
    }
}
