package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class CommentDtoTest {
    private JacksonTester<CommentDto> json;
    private CommentDto commentDto;

    public CommentDtoTest(@Autowired JacksonTester<CommentDto> json) {
        this.json = json;
    }

    @BeforeEach
    void beforeEach() {
        commentDto = new CommentDto(
                1L,
                "comment",
                "author",
                LocalDateTime.of(2030, 12, 26, 12, 00)
        );
    }

    @Test
    void testJsonCommentDto() throws Exception {
        JsonContent<CommentDto> result = json.write(commentDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("comment");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("author");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2030-12-26T12:00:00");
    }
}
