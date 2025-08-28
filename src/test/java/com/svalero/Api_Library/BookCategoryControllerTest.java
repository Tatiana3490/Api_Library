package com.svalero.Api_Library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.Api_Library.controller.BookCategoryController;
import com.svalero.Api_Library.domain.BookCategory;
import com.svalero.Api_Library.exception.BookCategoryNotFoundException;
import com.svalero.Api_Library.security.JwtAuthenticationFilter;
import com.svalero.Api_Library.security.JwtRequestFilter;
import com.svalero.Api_Library.service.BookCategoryService;
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
import static org.springframework.data.jpa.domain.AbstractAuditable_.createdDate;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BookCategoryController.class,
        // Igual que en BookControllerTest: sacamos filtros JWT del slice
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtRequestFilter.class, JwtAuthenticationFilter.class }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class BookCategoryControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    // Servicio mockeado que usa el controller
    @MockBean
    BookCategoryService service;

    // Mocks de seguridad por si cuela alguna referencia
    @MockBean
    JwtRequestFilter jwtRequestFilter;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    // Helper para crear categorías sin repetir setters
    private BookCategory cat(long id, String name, String description,
                             boolean active, LocalDate createdDate, int numberBooks) {
        BookCategory c = new BookCategory();
        c.setId(id);
        c.setName(name);
        c.setDescription(description);
        c.setActive(active);
        c.setCreatedDate(createdDate);
        c.setNumberBooks(numberBooks);
        return c;
    }
    // ===================== GET básicos =====================

    @Test
    @DisplayName("GET /book-categories -> 200 OK y lista")
    void getAll_Returns200() throws Exception {
        when(service.getAllBookCategories()).thenReturn(List.of(
                cat(1, "Sci-Fi", "Ficción científica", true, LocalDate.parse("2020-01-01"), 12),
                cat(2, "Fantasy", "Fantasía épica", true, LocalDate.parse("2021-06-15"), 20)
        ));

        mockMvc.perform(get("/book-categories").accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sci-Fi"))
                .andExpect(jsonPath("$[1].name").value("Fantasy"));

        verify(service).getAllBookCategories();
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /book-categories/{id} -> 200 OK")
    void getById_Returns200() throws Exception {
        when(service.getBookCategoriesById(1L))
                .thenReturn(cat(1,"Sci-Fi","Ficción científica", true, LocalDate.parse("2020-01-01"), 12));

        mockMvc.perform(get("/book-categories/{id}", 1L).accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sci-Fi"))
                .andExpect(jsonPath("$.numberBooks").value(12));

        verify(service).getBookCategoriesById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /book-categories/{id} -> 404 si no existe")
    void getById_Returns404() throws Exception {
        when(service.getBookCategoriesById(999L))
                .thenThrow(new BookCategoryNotFoundException("not found"));

        mockMvc.perform(get("/book-categories/{id}", 999L).accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(service).getBookCategoriesById(999L);
        verifyNoMoreInteractions(service);
    }

    // ===================== GET con filtros =====================

    @Test
    @DisplayName("GET /book-categories/name?name=... -> 200 OK")
    void getByName_Returns200() throws Exception {
        when(service.getBookCategoriesByName("Sci-Fi")).thenReturn(List.of(
                cat(1,"Sci-Fi","Ficción científica", true, LocalDate.parse("2020-01-01"), 12)
        ));

        mockMvc.perform(get("/book-categories/name").queryParam("name", "Sci-Fi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sci-Fi"));

        verify(service).getBookCategoriesByName("Sci-Fi");
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /book-categories/description?description=... -> 200 OK")
    void getByDescription_Returns200() throws Exception {
        when(service.getBookCategoriesByDescription("Ficción"))
                .thenReturn(List.of(cat(1,"Sci-Fi","Ficción científica", true, LocalDate.parse("2020-01-01"), 12)));

        mockMvc.perform(get("/book-categories/description").queryParam("description", "Ficción"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value(org.hamcrest.Matchers.containsString("Ficción")));

        verify(service).getBookCategoriesByDescription("Ficción");
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /book-categories/active?active=true -> 200 OK")
    void getByActive_Returns200() throws Exception {
        when(service.getBookCategoriesByActive(true))
                .thenReturn(List.of(cat(1,"Sci-Fi","Ficción científica", true, LocalDate.parse("2020-01-01"), 12)));

        mockMvc.perform(get("/book-categories/active").queryParam("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));

        verify(service).getBookCategoriesByActive(true);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /book-categories/created-date?date=yyyy-MM-dd -> 200 OK")
    void getByCreationDate_Returns200() throws Exception {
        LocalDate date = LocalDate.parse("2020-01-01");
        when(service.getBookCategoriesByCreateDate(date))
                .thenReturn(List.of(cat(1,"Sci-Fi","Ficción científica", true, date, 12)));

        mockMvc.perform(get("/book-categories/creation-date").queryParam("date", "2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].createdDate").value("2020-01-01"));

        verify(service).getBookCategoriesByCreateDate(date);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /book-categories/number-books?numberBooks=... -> 200 OK")
    void getByNumberBooks_Returns200() throws Exception {
        when(service.getBookCategoriesByNumberBooks(10))
                .thenReturn(List.of(cat(3,"Terror","Miedo y suspense", true, LocalDate.parse("2019-10-31"), 10)));

        mockMvc.perform(get("/book-categories/number-books").queryParam("numberBooks", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numberBooks").value(10));

        verify(service).getBookCategoriesByNumberBooks(10);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /book-categories/min-books?minBooks=... -> 200 OK")
    void getWithMinBooks_Returns200() throws Exception {
        when(service.getBookCategoriesWithMinBooks(15))
                .thenReturn(List.of(cat(2,"Fantasy","Fantasía épica", true, LocalDate.parse("2021-06-15"), 20)));

        mockMvc.perform(get("/book-categories/min-books").queryParam("minBooks", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numberBooks").value(20));

        verify(service).getBookCategoriesWithMinBooks(15);
        verifyNoMoreInteractions(service);
    }

    // ===================== POST =====================

    @Test
    @DisplayName("POST /book-categories -> 201 Created")
    void add_Returns201() throws Exception {
        BookCategory in = cat(0,"Sci-Fi","Ficción científica", true, LocalDate.parse("2020-01-01"), 12);
        BookCategory saved = cat(10,"Sci-Fi","Ficción científica", true, LocalDate.parse("2020-01-01"), 12);

        when(service.saveBookCategory(any(BookCategory.class))).thenReturn(saved);

        mockMvc.perform(post("/book-categories")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Sci-Fi"));

        verify(service).saveBookCategory(any(BookCategory.class));
        verifyNoMoreInteractions(service);
    }

    // La entidad lleva @Valid (ej. @NotBlank en name)
     @Test
     @DisplayName("POST /book-categories -> 400 Bad Request si faltan datos")
     void add_Returns400_WhenInvalid() throws Exception {
        BookCategory invalid = cat(0,"","desc", true, LocalDate.now(), 0);
        mockMvc.perform(post("/book-categories")
                         .contentType(APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(invalid)))
                 .andExpect(status().isBadRequest());
         verifyNoInteractions(service);
     }

    // ===================== PUT =====================

    @Test
    @DisplayName("PUT /book-categories/{id} -> 200 OK")
    void update_Returns200() throws Exception {
        BookCategory changes = cat(0,"Sci-Fi 2","Más sci-fi", false, LocalDate.parse("2020-01-01"), 15);
        BookCategory updated = cat(1,"Sci-Fi 2","Más sci-fi", false, LocalDate.parse("2020-01-01"), 15);

        when(service.updateBookCategory(eq(1L), any(BookCategory.class))).thenReturn(updated);

        mockMvc.perform(put("/book-categories/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sci-Fi 2"))
                .andExpect(jsonPath("$.active").value(false));

        verify(service).updateBookCategory(eq(1L), any(BookCategory.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("PUT /book-categories/{id} -> 404 si no existe")
    void update_Returns404_WhenNotFound() throws Exception {
        when(service.updateBookCategory(eq(999L), any(BookCategory.class)))
                .thenThrow(new BookCategoryNotFoundException("no existe"));

        mockMvc.perform(put("/book-categories/{id}", 999L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat(0,"X","Y", true, LocalDate.now(), 1))))
                .andExpect(status().isNotFound());

        verify(service).updateBookCategory(eq(999L), any(BookCategory.class));
        verifyNoMoreInteractions(service);
    }

    // ===================== PATCH =====================

    @Test
    @DisplayName("PATCH /book-categories/{id} -> 200 OK")
    void patch_Returns200() throws Exception {
        BookCategory patched = cat(1,"Sci-Fi","Ficción científica", false, LocalDate.parse("2020-01-01"), 99);

        when(service.updateBookCategoryPartial(eq(1L), anyMap())).thenReturn(patched);

        mockMvc.perform(patch("/book-categories/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false, "numberBooks", 99))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.numberBooks").value(99));

        verify(service).updateBookCategoryPartial(eq(1L), anyMap());
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("PATCH /book-categories/{id} -> 404 si no existe")
    void patch_Returns404_WhenNotFound() throws Exception {
        when(service.updateBookCategoryPartial(eq(999L), anyMap()))
                .thenThrow(new BookCategoryNotFoundException("no existe"));

        mockMvc.perform(patch("/book-categories/{id}", 999L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isNotFound());

        verify(service).updateBookCategoryPartial(eq(999L), anyMap());
        verifyNoMoreInteractions(service);
    }

    // ===================== DELETE =====================

    @Test
    @DisplayName("DELETE /book-categories/{id} -> 204 No Content")
    void delete_Returns204() throws Exception {
        doNothing().when(service).deleteBookCategory(1L);

        mockMvc.perform(delete("/book-categories/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(service).deleteBookCategory(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("DELETE /book-categories/{id} -> 404 si no existe")
    void delete_Returns404_WhenNotFound() throws Exception {
        doThrow(new BookCategoryNotFoundException("no existe"))
                .when(service).deleteBookCategory(999L);

        mockMvc.perform(delete("/book-categories/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(service).deleteBookCategory(999L);
        verifyNoMoreInteractions(service);
    }




}
