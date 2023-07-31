package ru.practicum.shareit.request;

import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private Integer id;
    private String description;
    private User requester;
    private LocalDateTime created;
}
