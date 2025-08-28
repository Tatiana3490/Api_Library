package com.svalero.Api_Library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.Api_Library.controller.AuthorController;
import com.svalero.Api_Library.domain.Author;
import com.svalero.Api_Library.exception.AuthorNotFoundException;
import com.svalero.Api_Library.security.JwtAuthenticationFilter;
import com.svalero.Api_Library.security.JwtRequestFilter;
import com.svalero.Api_Library.service.AuthorService;
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
        controllers = AuthorController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtRequestFilter.class, JwtAuthenticationFilter.class }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthorControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthorService authorService;

    // Por si algún bean de seguridad se cuela
    @MockBean
    JwtRequestFilter jwtRequestFilter;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    // Helper para construir autores
    private Author a(long id, String name, String surname, String nationality, LocalDate birthdate) {
        Author au = new Author();
        au.setId(id);
        au.setName(name);
        au.setSurname(surname);
        au.setNationality(nationality);
        au.setBirthdate(birthdate);
        return au;
    }

    // =============== GET: lista =================

    @Test
    @DisplayName("GET /authors -> 200 OK y lista")
    void getAllAuthors_Returns200() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(List.of(
                a(1, "Frank", "Herbert", "USA", LocalDate.parse("1920-10-08")),
                a(2, "William", "Gibson", "Canada", LocalDate.parse("1948-03-17"))
        ));

        mockMvc.perform(get("/authors").accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Frank"))
                .andExpect(jsonPath("$[1].surname").value("Gibson"));

        verify(authorService).getAllAuthors();
        verifyNoMoreInteractions(authorService);
    }

    // =============== POST: creación ==============

    @Test
    @DisplayName("POST /authors -> 201 Created")
    void addAuthor_Returns201() throws Exception {
        Author in = a(0, "Isaac", "Asimov", "Russia/USA", LocalDate.parse("1920-01-02"));
        Author saved = a(10, "Isaac", "Asimov", "Russia/USA", LocalDate.parse("1920-01-02"));

        when(authorService.saveAuthor(any(Author.class))).thenReturn(saved);

        mockMvc.perform(post("/authors")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Isaac"));

        verify(authorService).saveAuthor(any(Author.class));
        verifyNoMoreInteractions(authorService);
    }

    @Test
    @DisplayName("POST /authors -> 400 Bad Request si faltan datos")
    void addAuthor_Returns400_WhenInvalid() throws Exception {
        // Ajusta si tu entidad no tiene estas validaciones
        Author invalid = a(0, "", "Surname", "ES", null);

        mockMvc.perform(post("/authors")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authorService);
    }

    // =============== GET: filtros simples ==============

    @Test
    @DisplayName("GET /authors/name?name=... -> 200 OK")
    void getByName_Returns200() throws Exception {
        when(authorService.getAuthorByName("Frank")).thenReturn(List.of(
                a(1, "Frank", "Herbert", "USA", LocalDate.parse("1920-10-08"))
        ));

        mockMvc.perform(get("/authors/name").queryParam("name", "Frank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].surname").value("Herbert"));

        verify(authorService).getAuthorByName("Frank");
        verifyNoMoreInteractions(authorService);
    }

    @Test
    @DisplayName("GET /authors/surname?surname=... -> 200 OK")
    void getBySurname_Returns200() throws Exception {
        when(authorService.getAuthorBySurname("Herbert")).thenReturn(List.of(
                a(1, "Frank", "Herbert", "USA", LocalDate.parse("1920-10-08"))
        ));

        mockMvc.perform(get("/authors/surname").queryParam("surname", "Herbert"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Frank"));

        verify(authorService).getAuthorBySurname("Herbert");
        verifyNoMoreInteractions(authorService);
    }

    @Test
    @DisplayName("GET /authors/nationality?nationality=... -> 200 OK")
    void getByNationality_Returns200() throws Exception {
        when(authorService.getAuthorByNationality("USA")).thenReturn(List.of(
                a(1, "Frank", "Herbert", "USA", LocalDate.parse("1920-10-08"))
        ));

        mockMvc.perform(get("/authors/nationality").queryParam("nationality", "USA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nationality").value("USA"));

        verify(authorService).getAuthorByNationality("USA");
        verifyNoMoreInteractions(authorService);
    }

    @Test
    @DisplayName("GET /authors/birthday?birthday=yyyy-MM-dd -> 200 OK")
    void getByBirthday_Returns200() throws Exception {
        LocalDate date = LocalDate.parse("1920-10-08");
        when(authorService.getAuthorByBirthdate(date)).thenReturn(List.of(
                a(1, "Frank", "Herbert", "USA", date)
        ));

        mockMvc.perform(get("/authors/birthday").queryParam("birthday", "1920-10-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].birthdate").value("1920-10-08"));

        verify(authorService).getAuthorByBirthdate(date);
        verifyNoMoreInteractions(authorService);
    }

    // =============== GET: por id ==============

    @Test
    @DisplayName("GET /authors/{id} -> 200 OK")
    void getById_Returns200() throws Exception {
        when(authorService.getAuthorById(1L))
                .thenReturn(a(1, "Frank", "Herbert", "USA", LocalDate.parse("1920-10-08")));

        mockMvc.perform(get("/authors/{id}", 1L).accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Frank"))
                .andExpect(jsonPath("$.surname").value("Herbert"));

        verify(authorService).getAuthorById(1L);
        verifyNoMoreInteractions(authorService);
    }

    @Test
    @DisplayName("GET /authors/{id} -> 404 si no existe")
    void getById_Returns404_WhenNotFound() throws Exception {
        when(authorService.getAuthorById(999L))
                .thenThrow(new AuthorNotFoundException("not found"));

        mockMvc.perform(get("/authors/{id}", 999L).accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(authorService).getAuthorById(999L);
        verifyNoMoreInteractions(authorService);
    }

    // =============== PUT: actualización completa ==============

    @Test
    @DisplayName("PUT /authors/{id} -> 200 OK")
    void updateAuthor_Returns200() throws Exception {
        Author changes = a(0, "Franklin", "Herbert", "USA", LocalDate.parse("1920-10-08"));
        Author updated = a(1, "Franklin", "Herbert", "USA", LocalDate.parse("1920-10-08"));

        when(authorService.updateAuthor(eq(1L), any(Author.class))).thenReturn(updated);

        mockMvc.perform(put("/authors/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Franklin"));

        verify(authorService).updateAuthor(eq(1L), any(Author.class));
        verifyNoMoreInteractions(authorService);
    }

    @Test
    @DisplayName("PUT /authors/{id} -> 404 si no existe")
    void updateAuthor_Returns404_WhenNotFound() throws Exception {
        when(authorService.updateAuthor(eq(999L), any(Author.class)))
                .thenThrow(new AuthorNotFoundException("no existe"));

        mockMvc.perform(put("/authors/{id}", 999L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(a(0, "X", "Y", "Z", LocalDate.parse("2000-01-01")))))
                .andExpect(status().isNotFound());

        verify(authorService).updateAuthor(eq(999L), any(Author.class));
        verifyNoMoreInteractions(authorService);
    }

    @Test
    @DisplayName("PUT /authors/{id} -> 400 si datos inválidos")
    void updateAuthor_Returns400_WhenInvalid() throws Exception {
        Author invalid = a(0, "", "Herbert", "USA", null);

        mockMvc.perform(put("/authors/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoMoreInteractions(authorService);
    }

    // =============== PATCH: actualización parcial ==============

    @Test
    @DisplayName("PATCH /authors/{id} -> 200 OK")
    void patchAuthor_Returns200() throws Exception {
        Author patched = a(1, "Frank", "Herbert", "USA", LocalDate.parse("1920-10-08"));
        when(authorService.updateAuthorPartial(eq(1L), anyMap())).thenReturn(patched);

        mockMvc.perform(patch("/authors/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Frank"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Frank"));

        verify(authorService).updateAuthorPartial(eq(1L), anyMap());
        verifyNoMoreInteractions(authorService);
    }

    @Test
    @DisplayName("PATCH /authors/{id} -> 404 si no existe")
    void patchAuthor_Returns404_WhenNotFound() throws Exception {
        when(authorService.updateAuthorPartial(eq(999L), anyMap()))
                .thenThrow(new AuthorNotFoundException("no existe"));

        mockMvc.perform(patch("/authors/{id}", 999L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "X"))))
                .andExpect(status().isNotFound());

        verify(authorService).updateAuthorPartial(eq(999L), anyMap());
        verifyNoMoreInteractions(authorService);
    }

    // =============== DELETE: borrado ==============

    @Test
    @DisplayName("DELETE /authors/{id} -> 204 No Content")
    void deleteAuthor_Returns204() throws Exception {
        doNothing().when(authorService).deleteAuthor(1L);

        mockMvc.perform(delete("/authors/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(authorService).deleteAuthor(1L);
        verifyNoMoreInteractions(authorService);
    }

    @Test
    @DisplayName("DELETE /authors/{id} -> 404 si no existe")
    void deleteAuthor_Returns404_WhenNotFound() throws Exception {
        doThrow(new AuthorNotFoundException("no existe")).when(authorService).deleteAuthor(999L);

        mockMvc.perform(delete("/authors/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(authorService).deleteAuthor(999L);
        verifyNoMoreInteractions(authorService);
    }



}