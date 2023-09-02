package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(long userId, ItemDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> updateItem(long userId, ItemDto dto, long itemId) {
        String path = String.format("/%d", itemId);
        return patch(path, userId, dto);
    }

    public ResponseEntity<Object> getItemById(long itemId, long userId) {
        String path = String.format("/%d", itemId);
        return get(path, userId);
    }

    public ResponseEntity<Object> getItemsForUser(long userId, int from, int size) {
        Map<String, Object> params = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, params);
    }

    public ResponseEntity<Object> searchItems(String query, int from, int size) {
        Map<String, Object> params = Map.of(
                "text", query,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", null, params);
    }

    public ResponseEntity<Object> postComment(long itemId, long userId, CommentDto dto) {
        String path = String.format("/%d/comment", itemId);
        return post(path, userId, dto);
    }
}
