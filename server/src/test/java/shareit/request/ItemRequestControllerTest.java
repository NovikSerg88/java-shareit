package shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserDto;

@WebMvcTest(controllers = ItemRequestController.class)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private UserDto userDto = new UserDto(1L, "user", "user@user.ru");
    private ItemRequestResponseDto itemRequestResponseDto = new ItemRequestResponseDto(1L, "ItemRequest description",
            LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);
    private ItemRequestDto itemRequestDto = new ItemRequestDto("ItemRequest description", userDto.getId());
    private List<ItemResponseDto> listItemRequestDto = null;

    @Test
    void createItemRequest() throws Exception {
        when(itemRequestService.create(any(), any(Long.class)))
                .thenReturn(itemRequestResponseDto);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.created",
                        is(itemRequestResponseDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.items", is(listItemRequestDto)));
    }

    @Test
    void getItemRequest() throws Exception {
        when(itemRequestService.getRequestById(any(Long.class), any(Long.class)))
                .thenReturn(itemRequestResponseDto);
        mvc.perform(get("/requests/1")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.created",
                        is(itemRequestResponseDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.items", is(listItemRequestDto)));
    }

    @Test
    void getAllUserRequests() throws Exception {
        when(itemRequestService.getAllUserRequests(any(Long.class)))
                .thenReturn(List.of(itemRequestResponseDto));
        mvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(listItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.[0].created",
                        is(itemRequestResponseDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
    }

    @Test
    void getItemRequests() throws Exception {
        when(itemRequestService.getAllRequests(any(Long.class), any(Integer.class), nullable(Integer.class)))
                .thenReturn(List.of(itemRequestResponseDto));
        mvc.perform(get("/requests/all")
                        .content(mapper.writeValueAsString(listItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.[0].created",
                        is(itemRequestResponseDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
    }
}