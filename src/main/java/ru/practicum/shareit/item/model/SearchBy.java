package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SearchBy {
    NAME("name"),
    DESCRIPTION("description"),
    AVAILABLE("available");

    private final String columnName;
}
