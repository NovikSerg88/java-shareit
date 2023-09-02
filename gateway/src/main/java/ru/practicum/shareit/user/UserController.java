package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@RestController
public class UserController {

    private final UserClient userClient;

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable("userId") long userId,
                                             @RequestBody UserDto dto) {
        log.info("Received PATCH request to update user with id={}. User to update={}",
                userId,
                dto);
        return userClient.updateUser(userId, dto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteById(@PathVariable("userId") long userId) {
        log.info("Received request to DELETE user by id={}", userId);
        return userClient.deleteById(userId);
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserDto dto) {
        log.info("Received request to POST user={}", dto);
        return userClient.createUser(dto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable("userId") long userId) {
        log.info("Received request to GET user by id={}", userId);
        return userClient.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Received request to GET all users.");
        return userClient.getAll();
    }
}
