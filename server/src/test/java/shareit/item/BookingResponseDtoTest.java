package shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.BookingResponseDto;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingResponseDtoTest {
    private JacksonTester<BookingResponseDto> json;
    private BookingResponseDto bookingResponseDto;

    public BookingResponseDtoTest(@Autowired JacksonTester<BookingResponseDto> json) {
        this.json = json;
    }

    @BeforeEach
    void beforeEach() {
        bookingResponseDto = new BookingResponseDto(
                1L,
                1L
        );
    }

    @Test
    void testJsonCommentDto() throws Exception {
        JsonContent<BookingResponseDto> result = json.write(bookingResponseDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(1);
    }
}
