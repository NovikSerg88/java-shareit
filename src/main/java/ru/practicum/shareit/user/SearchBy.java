package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SearchBy {
    NAME("name"),
    EMAIL("email");

    private final String columnName;
}
