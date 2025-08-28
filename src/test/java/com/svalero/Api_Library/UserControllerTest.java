package com.svalero.Api_Library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.Api_Library.controller.UserController;
import com.svalero.Api_Library.domain.User;
import com.svalero.Api_Library.exception.UserNotFoundException;
import com.svalero.Api_Library.security.JwtAuthenticationFilter;
import com.svalero.Api_Library.security.JwtRequestFilter;
import com.svalero.Api_Library.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        // Igual que en otros controllers: sacamos los filtros JWT del slice
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtRequestFilter.class, JwtAuthenticationFilter.class }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    // Mocks de seguridad por si cuela alguna referencia
    @MockBean
    JwtRequestFilter jwtRequestFilter;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    // -------- Helpers --------
    private User u(Long id, String name, String username, String email,
                   String password, LocalDate creationDate, Boolean active) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setCreationDate(creationDate);
        user.setActive(active);
        return user;
    }

    // Para el POST usamos el DTO, pero como no dependemos de su clase,
    // generamos el JSON con un Map (las propiedades deben llamarse igual que en el DTO)
    private String userInDtoJson(String name, String username, String email,
                                 String password, String creationDate, boolean active) throws Exception {
        Map<String, Object> payload = Map.of(
                "name", name,
                "username", username,
                "email", email,
                "password", password,
                "creationDate", creationDate,
                "active", active
        );
        return objectMapper.writeValueAsString(payload);
    }

    // =================== GET: listado ===================

    @Test
    @DisplayName("GET /users -> 200 OK y lista")
    void getAllUsers_Returns200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                u(1L, "Frank Herbert", "fherbert", "frank@example.com", "Password#123", LocalDate.parse("2020-01-01"), true),
                u(2L, "William Gibson", "wgibson", "william@example.com", "Password#123", LocalDate.parse("2020-01-02"), true)
        ));

        mockMvc.perform(get("/users").accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("fherbert"))
                .andExpect(jsonPath("$[1].email").value("william@example.com"));

        verify(userService).getAllUsers();
        verifyNoMoreInteractions(userService);
    }

    // =================== GET: por email ===================

    @Test
    @DisplayName("GET /users/email?email=... -> 200 OK")
    void getByEmail_Returns200() throws Exception {
        when(userService.getUserByEmail("frank@example.com"))
                .thenReturn(u(1L, "Frank Herbert", "fherbert", "frank@example.com", "Password#123", LocalDate.parse("2020-01-01"), true));

        mockMvc.perform(get("/users/email").queryParam("email", "frank@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("fherbert"));

        verify(userService).getUserByEmail("frank@example.com");
        verifyNoMoreInteractions(userService);
    }

    // =================== GET: por username ===================

    @Test
    @DisplayName("GET /users/username?username=... -> 200 OK")
    void getByUsername_Returns200() throws Exception {
        when(userService.getUserByUsername("fherbert"))
                .thenReturn(u(1L, "Frank Herbert", "fherbert", "frank@example.com", "Password#123", LocalDate.parse("2020-01-01"), true));

        mockMvc.perform(get("/users/username").queryParam("username", "fherbert"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("frank@example.com"));

        verify(userService).getUserByUsername("fherbert");
        verifyNoMoreInteractions(userService);
    }

    // =================== GET: activos ===================

    @Test
    @DisplayName("GET /users/active -> 200 OK con lista")
    void getActiveUsers_Returns200() throws Exception {
        when(userService.getActiveUsers()).thenReturn(List.of(
                u(1L, "Frank Herbert", "fherbert", "frank@example.com", "Password#123", LocalDate.parse("2020-01-01"), true)
        ));

        mockMvc.perform(get("/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));

        verify(userService).getActiveUsers();
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("GET /users/active -> 204 No Content cuando no hay usuarios")
    void getActiveUsers_Returns204_WhenEmpty() throws Exception {
        when(userService.getActiveUsers()).thenReturn(List.of());

        mockMvc.perform(get("/users/active"))
                .andExpect(status().isNoContent());

        verify(userService).getActiveUsers();
        verifyNoMoreInteractions(userService);
    }

    // =================== GET: search (name contains) ===================

    @Test
    @DisplayName("GET /users/search?name=... -> 200 OK")
    void searchByName_Returns200() throws Exception {
        when(userService.findUsersByNameContaining("Frank")).thenReturn(List.of(
                u(1L, "Frank Herbert", "fherbert", "frank@example.com", "Password#123", LocalDate.parse("2020-01-01"), true)
        ));

        mockMvc.perform(get("/users/search").queryParam("name", "Frank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Frank Herbert"));

        verify(userService).findUsersByNameContaining("Frank");
        verifyNoMoreInteractions(userService);
    }

    // =================== POST: creación (DTO) ===================

    @Test
    @DisplayName("POST /users -> 201 Created")
    void addUser_Returns201() throws Exception {
        User saved = u(10L, "Isaac Asimov", "iasimov", "isaac@example.com", "Password#123",
                LocalDate.parse("2020-01-03"), true);

        // El service recibe UserInDto y devuelve User
        when(userService.saveUser(any())).thenReturn(saved);

        String body = userInDtoJson(
                "Isaac Asimov",
                "iasimov",
                "isaac@example.com",
                "Password#123",
                "2020-01-03",
                true
        );

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("iasimov"));

        verify(userService).saveUser(any());
        verifyNoMoreInteractions(userService);
    }

    // =================== PUT: actualización completa ===================

    @Test
    @DisplayName("PUT /users/{id} -> 200 OK")
    void updateUser_Returns200() throws Exception {
        User changes = u(null, "Franklin Herbert", "fherbert", "frank@example.com",
                "Password#123", LocalDate.parse("2020-01-01"), true);
        User updated = u(1L, "Franklin Herbert", "fherbert", "frank@example.com",
                "Password#123", LocalDate.parse("2020-01-01"), true);

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/users/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Franklin Herbert"));

        verify(userService).updateUser(eq(1L), any(User.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("PUT /users/{id} -> 404 si no existe")
    void updateUser_Returns404_WhenNotFound() throws Exception {
        when(userService.updateUser(eq(999L), any(User.class)))
                .thenThrow(new UserNotFoundException("not found"));

        mockMvc.perform(put("/users/{id}", 999L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                u(null, "X", "xuser", "x@example.com", "Password#123",
                                        LocalDate.parse("2020-01-01"), true))))
                .andExpect(status().isNotFound());

        verify(userService).updateUser(eq(999L), any(User.class));
        verifyNoMoreInteractions(userService);
    }

    // --- PUT: 400 por @Valid del entity User ---
    @Test
    @DisplayName("PUT /users/{id} -> 400 si email no es válido")
    void updateUser_Returns400_WhenEmailInvalid() throws Exception {
        User invalid = u(null, "Frank Herbert", "fherbert",
                "no-es-un-email",            // inválido por @Email
                "Password#123",
                LocalDate.parse("2020-01-01"),
                true);

        mockMvc.perform(put("/users/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("PUT /users/{id} -> 400 si faltan campos o password es corta")
    void updateUser_Returns400_WhenBlankOrShortPassword() throws Exception {
        User invalid = u(null, "",        // name en blanco -> @NotBlank
                "  ",                     // username en blanco -> @NotBlank
                "frank@example.com",
                "123",                    // @Size(min = 8)
                LocalDate.parse("2020-01-01"),
                true);

        mockMvc.perform(put("/users/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // =================== PATCH: actualización parcial ===================

    @Test
    @DisplayName("PATCH /users/{id} -> 200 OK")
    void patchUser_Returns200() throws Exception {
        User patched = u(1L, "Frank Herbert", "fherbert", "frank@example.com",
                "Password#123", LocalDate.parse("2020-01-01"), false);

        when(userService.updateUserPartial(eq(1L), anyMap())).thenReturn(patched);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(userService).updateUserPartial(eq(1L), anyMap());
        verifyNoMoreInteractions(userService);
    }

    // =================== DELETE: borrado ===================

    @Test
    @DisplayName("DELETE /users/{id} -> 204 No Content")
    void deleteUser_Returns204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("DELETE /users/{id} -> 404 si no existe")
    void deleteUser_Returns404_WhenNotFound() throws Exception {
        doThrow(new UserNotFoundException("not found")).when(userService).deleteUser(999L);

        mockMvc.perform(delete("/users/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(999L);
        verifyNoMoreInteractions(userService);
    }
}
