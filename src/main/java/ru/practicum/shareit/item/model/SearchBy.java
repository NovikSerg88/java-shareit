package ru.practicum.shareit.item.model;

public enum SearchBy {
    name("name"),
    description("description"),
    available("available");

    private final String columnName;

    SearchBy(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
