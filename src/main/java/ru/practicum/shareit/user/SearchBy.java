package ru.practicum.shareit.user;

public enum SearchBy {
    name("name"),
    email("email");

    private final String columnName;

    SearchBy(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
