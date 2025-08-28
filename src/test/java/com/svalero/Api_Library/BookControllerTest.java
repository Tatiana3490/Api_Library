package com.svalero.Api_Library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.Api_Library.controller.BookController;
import com.svalero.Api_Library.domain.Book;
import com.svalero.Api_Library.exception.BookNotFoundException;
import com.svalero.Api_Library.security.JwtAuthenticationFilter;
import com.svalero.Api_Library.security.JwtRequestFilter;
import com.svalero.Api_Library.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BookController.class,
        // Evitamos que se registren los filtros de JWT en este slice test
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtRequestFilter.class, JwtAuthenticationFilter.class }
        )
)
@AutoConfigureMockMvc(addFilters = false) // no aplicar filtros a MockMvc
class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // mock del servicio usado por el controller
    @MockBean
    BookService bookService;

    // mocks por si alguna referencia al filtro cuela
    @MockBean
    JwtRequestFilter jwtRequestFilter;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    private Book b(long id, String title, String genre, int pages, float price, boolean available) {
        return new Book(id, title, genre, pages, price, available, null, null, null);
    }

    @Test
    @DisplayName("GET /books -> 200 OK y lista")
    void getAllBooks_Returns200() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(
                b(1L, "Dune", "Sci-Fi", 600, 29.9f, true),
                b(2L, "Neuromancer", "Sci-Fi", 300, 19.9f, true)
        ));

        mockMvc.perform(get("/books").accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Dune"))
                .andExpect(jsonPath("$[1].title").value("Neuromancer"));

        verify(bookService).getAllBooks();
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("GET /books/{id} -> 200 OK")
    void getBookById_Returns200() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(b(1,"Dune", "Sci-Fi", 600, 29.9f, true));
        mockMvc.perform(get("/books/{id}", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"))
                .andExpect(jsonPath("$.genre").value("Sci-Fi"))
                .andExpect(jsonPath("$.pages").value(600))
                .andExpect(jsonPath("$.price").value(29.9f))
                .andExpect(jsonPath("$.available").value(true));

        verify(bookService).getBookById(1L);
        verifyNoMoreInteractions(bookService);

    }

    @Test
    @DisplayName("GET /books/{id} -> 404 cuando no existe")
    void getBookById_Returns404() throws Exception {
        when(bookService.getBookById(999L)).thenThrow(new BookNotFoundException("not found"));

        mockMvc.perform(get("/books/{id}", 999L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookService).getBookById(999L);
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("GET /books/title?title=... -> 200 OK")
    void getBooksByTitle_Returns200() throws Exception {
        when(bookService.getBookByTitle("Dune")).thenReturn(List.of(
                b(1, "Dune", "Sci-Fi", 600, 29.9f, true)
        ));

        mockMvc.perform(get("/books/title").queryParam("title", "Dune"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Dune"));

        verify(bookService).getBookByTitle("Dune");
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("GET /books/genre?genre=... -> 200 OK")
    void getBooksByGenre_Returns200() throws Exception {
        when(bookService.getBookByGenre("Sci-Fi")).thenReturn(List.of(
                b(1, "Dune", "Sci-Fi", 600, 29.9f, true),
                b(2, "Neuromancer", "Sci-Fi", 300, 19.9f, true)
        ));

        mockMvc.perform(get("/books/genre").queryParam("genre", "Sci-Fi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genre").value("Sci-Fi"))
                .andExpect(jsonPath("$[1].genre").value("Sci-Fi"));

        verify(bookService).getBookByGenre("Sci-Fi");
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("GET /books/available?available=true -> 200 OK")
    void getBooksByAvailable_Returns200() throws Exception {
        when(bookService.getBookByAvailability(true)).thenReturn(List.of(
                b(1, "Dune", "Sci-Fi", 600, 29.9f, true)
        ));

        mockMvc.perform(get("/books/available").queryParam("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(true));

        verify(bookService).getBookByAvailability(true);
        verifyNoMoreInteractions(bookService);
    }


    // ---------------------- GET combinados ----------------------

    @Test
    @DisplayName("GET /books/search?title=..&available=..&genre=.. -> 200 OK")
    void getBooksByTitleAndAvailableAndGenre_Returns200() throws Exception {
        when(bookService.findBooksByTitleAndAvailableAndGenre("Dune", true, "Sci-Fi"))
                .thenReturn(List.of(b(1, "Dune", "Sci-Fi", 600, 29.9f, true)));

        mockMvc.perform(get("/books/search")
                        .queryParam("title", "Dune")
                        .queryParam("available", "true")
                        .queryParam("genre", "Sci-Fi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Dune"));

        verify(bookService).findBooksByTitleAndAvailableAndGenre("Dune", true, "Sci-Fi");
        verifyNoMoreInteractions(bookService);
    }

    // ---------------------- GET JPQL ----------------------

    @Test
    @DisplayName("GET /books/pages-greater-than?pages=... -> 200 OK")
    void getBooksWithPagesGreaterThan_Returns200() throws Exception {
        when(bookService.findBooksWithPagesGreaterThan(400)).thenReturn(List.of(
                b(1, "Dune", "Sci-Fi", 600, 29.9f, true)
        ));

        mockMvc.perform(get("/books/pages-greater-than").queryParam("pages", "400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pages").value(600));

        verify(bookService).findBooksWithPagesGreaterThan(400);
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("GET /books/price-less-than?price=... -> 200 OK")
    void getBooksWithPriceLessThan_Returns200() throws Exception {
        when(bookService.findBooksWithPriceLessThan(25f)).thenReturn(List.of(
                b(2, "Neuromancer", "Sci-Fi", 300, 19.9f, true)
        ));

        mockMvc.perform(get("/books/price-less-than").queryParam("price", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(19.9f));

        verify(bookService).findBooksWithPriceLessThan(25f);
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("GET /books/genre-contains?keyword=.. -> 200 OK")
    void getBooksWithGenreLike_Returns200() throws Exception {
        when(bookService.findBooksWithGenreLike("Fi")).thenReturn(List.of(
                b(1, "Dune", "Sci-Fi", 600, 29.9f, true)
        ));

        mockMvc.perform(get("/books/genre-contains").queryParam("keyword", "Fi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genre").value("Sci-Fi"));

        verify(bookService).findBooksWithGenreLike("Fi");
        verifyNoMoreInteractions(bookService);
    }

    // ---------------------- POST ----------------------

    @Test
    @DisplayName("POST /books -> 201 Created")
    void addBook_Returns201() throws Exception {
        Book in = b(0, "Dune", "Sci-Fi", 600, 29.9f, true);
        Book saved = b(10, "Dune", "Sci-Fi", 600, 29.9f, true);

        when(bookService.saveBook(any(Book.class))).thenReturn(saved);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Dune"));

        verify(bookService).saveBook(any(Book.class));
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("POST /books -> 400 Bad Request si faltan datos")
    void addBook_Returns400_WhenMissingTitle() throws Exception {
        // Title vacío para forzar @Valid (si el entity tiene @NotBlank en title)
        Book invalid = b(0, "", "Sci-Fi", 300, 19.9f, true);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
        // No se llama al servicio si falla la validación
        verifyNoInteractions(bookService);
    }

    // ---------------------- PUT ----------------------

    @Test
    @DisplayName("PUT /books/{id} -> 200 OK")
    void updateBook_Returns200() throws Exception {
        Book changes = b(0, "Dune (rev)", "Sci-Fi", 610, 31.0f, true);
        Book updated = b(1, "Dune (rev)", "Sci-Fi", 610, 31.0f, true);

        when(bookService.updateBook(eq(1L), any(Book.class))).thenReturn(updated);

        mockMvc.perform(put("/books/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune (rev)"))
                .andExpect(jsonPath("$.pages").value(610))
                .andExpect(jsonPath("$.price").value(31.0f));

        verify(bookService).updateBook(eq(1L), any(Book.class));
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("PUT /books/{id} -> 404 si no existe")
    void updateBook_Returns404_WhenNotFound() throws Exception {
        when(bookService.updateBook(eq(999L), any(Book.class))).thenThrow(new BookNotFoundException("no existe"));

        mockMvc.perform(put("/books/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(b(0, "X", "Y", 10, 1f, true))))
                .andExpect(status().isNotFound());

        verify(bookService).updateBook(eq(999L), any(Book.class));
        verifyNoMoreInteractions(bookService);
    }

    // ---------------------- PATCH ----------------------

    @Test
    @DisplayName("PATCH /books/{id} -> 200 OK")
    void patchBook_Returns200() throws Exception {
        Book patched = b(1, "Dune", "Sci-Fi", 600, 25.0f, true);
        when(bookService.updateBookPartial(eq(1L), anyMap())).thenReturn(patched);

        mockMvc.perform(patch("/books/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("price", 25.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(25.0));

        verify(bookService).updateBookPartial(eq(1L), anyMap());
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("PATCH /books/{id} -> 404 si no existe")
    void patchBook_Returns404_WhenNotFound() throws Exception {
        when(bookService.updateBookPartial(eq(999L), anyMap())).thenThrow(new BookNotFoundException("no existe"));

        mockMvc.perform(patch("/books/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("price", 25.0))))
                .andExpect(status().isNotFound());

        verify(bookService).updateBookPartial(eq(999L), anyMap());
        verifyNoMoreInteractions(bookService);
    }

    // ---------------------- DELETE ----------------------

    @Test
    @DisplayName("DELETE /books/{id} -> 204 No Content")
    void deleteBook_Returns204() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/books/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(1L);
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("DELETE /books/{id} -> 404 si no existe")
    void deleteBook_Returns404_WhenNotFound() throws Exception {
        doThrow(new BookNotFoundException("no existe")).when(bookService).deleteBook(999L);

        mockMvc.perform(delete("/books/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(bookService).deleteBook(999L);
        verifyNoMoreInteractions(bookService);
    }

    // ---------------------- SQL nativa ----------------------

    @Test
    @DisplayName("GET /books/price-greater-than-native?price=.. -> 200 OK")
    void getBooksWithPriceGreaterThanNative_Returns200() throws Exception {
        when(bookService.findBooksWithPriceGreaterThanNative(20f)).thenReturn(List.of(
                b(1, "Dune", "Sci-Fi", 600, 29.9f, true)
        ));

        mockMvc.perform(get("/books/price-greater-than-native").queryParam("price", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(29.9f));

        verify(bookService).findBooksWithPriceGreaterThanNative(20f);
        verifyNoMoreInteractions(bookService);
    }

    // ---------------------- Subida CSV ----------------------

    @Test
    @DisplayName("POST /books/upload -> 200 OK con CSV válido")
    void uploadBooksFile_Returns200() throws Exception {
        // title,genre,pages,price,available
        String csv = "Dune,Sci-Fi,600,29.9,true\nNeuromancer,Sci-Fi,300,19.9,true\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        // el controller llamará a saveBook() por cada línea
        when(bookService.saveBook(any(Book.class))).thenAnswer(inv -> {
            Book in = inv.getArgument(0, Book.class);
            // devolver el mismo objeto simulando persistencia
            in.setId(in.getTitle().equals("Dune") ? 1L : 2L);
            return in;
        });

        mockMvc.perform(multipart("/books/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Libros cargados correctamente")));

        verify(bookService, times(2)).saveBook(any(Book.class));
        verifyNoMoreInteractions(bookService);
    }

    @Test
    @DisplayName("POST /books/upload -> 500 cuando el CSV es inválido")
    void uploadBooksFile_Returns500_OnInvalidCSV() throws Exception {
        // páginas no numéricas para forzar NumberFormatException dentro del controller
        String csv = "Dune,Sci-Fi,NOT_A_NUMBER,29.9,true\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/books/upload").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error al procesar el fichero")));

        verifyNoInteractions(bookService); // ni siquiera llega a guardar
    }
}

