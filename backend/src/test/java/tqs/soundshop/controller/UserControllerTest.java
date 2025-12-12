package tqs.soundshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tqs.soundshop.config.SecurityConfig;
import tqs.soundshop.dto.RegisterUserRequest;
import tqs.soundshop.dto.UserDto;
import tqs.soundshop.entities.User;
import tqs.soundshop.service.UserService;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void register_returnsCreatedUser() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                "Alice",
                "alice@example.com",
                "secret",
                Set.of(User.Role.RENTER)
        );

        UserDto responseDto = new UserDto(
                1L,
                "alice@example.com",
                "Alice",
                Set.of(User.Role.RENTER)
        );

        when(userService.register(any(RegisterUserRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(userService).register(any(RegisterUserRequest.class));
    }

    @Test
    void getById_returnsUser() throws Exception {
        UserDto dto = new UserDto(
                2L,
                "bob@example.com",
                "Bob",
                Set.of(User.Role.RENTER, User.Role.OWNER)
        );

        when(userService.getById(2L)).thenReturn(dto);

        mockMvc.perform(get("/api/users/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.name").value("Bob"));

        verify(userService).getById(2L);
    }

    @Test
    @WithMockUser(username = "me@example.com")
    void me_returnsCurrentUser() throws Exception {
        UserDto dto = new UserDto(
                3L,
                "me@example.com",
                "Me",
                Set.of(User.Role.RENTER)
        );

        when(userService.getByEmail("me@example.com")).thenReturn(dto);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.name").value("Me"));

        verify(userService).getByEmail("me@example.com");
    }
}
