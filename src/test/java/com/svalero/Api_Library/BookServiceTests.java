package com.svalero.Api_Library;

import com.svalero.Api_Library.domain.Book;
import com.svalero.Api_Library.exception.BookNotFoundException;
import com.svalero.Api_Library.repository.BookRepository;
import com.svalero.Api_Library.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de BookService usando Mockito (sin tocar la base de datos).
 * Patrón: Arrange (preparo), Act (ejecuto), Assert (compruebo) + verify() del repo.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTests {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    // Helper para crear libros rápido
    private Book b(long id, String title, String genre, int pages, double price, boolean available) {
        return new Book(id, title, genre, pages, price, available, null, null, null);
    }

    // ===================== READ =====================

    @Test
    @DisplayName("getAllBooks devuelve todos los libros")
    void getAllBooks_returnsAll() {
        List<Book> mock = List.of(
                b(1, "El libro", "Poesia", 200, 10.5, true),
                b(2, "El libro 2", "Narrativa", 100, 11.5, true),
                b(3, "El libro 3", "Prosa", 50, 9.5, true)
        );
        when(bookRepository.findAll()).thenReturn(mock);

        List<Book> result = bookService.getAllBooks();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("El libro");

        verify(bookRepository).findAll();
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("getBookById devuelve el libro cuando existe")
    void getBookById_found() throws BookNotFoundException {
        when(bookRepository.findById(7L))
                .thenReturn(Optional.of(b(7, "Clean Code", "Tech", 450, 29.9, true)));

        Book result = bookService.getBookById(7L);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getTitle()).isEqualTo("Clean Code");

        verify(bookRepository).findById(7L);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("getBookById lanza excepción si no existe")
    void getBookById_notFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getBookById(99L));

        verify(bookRepository).findById(99L);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Filtros simples delegan en el repo")
    void simpleFilters_delegateToRepository() {
        when(bookRepository.findByTitle("Duna"))
                .thenReturn(List.of(b(1, "Duna", "Sci-Fi", 500, 12.0, true)));
        when(bookRepository.findByGenre("Sci-Fi"))
                .thenReturn(List.of(b(2, "Neuromante", "Sci-Fi", 300, 11.0, true)));
        when(bookRepository.findByPages(123))
                .thenReturn(List.of(b(3, "Corto", "Poesia", 123, 5.0, true)));
        when(bookRepository.findByPrice(15.5))
                .thenReturn(List.of(b(4, "Precio exacto", "Ensayo", 200, 15.5, true)));
        when(bookRepository.findByAvailable(true))
                .thenReturn(List.of(b(5, "Disponible", "Drama", 250, 9.0, true)));

        assertThat(bookService.getBookByTitle("Duna")).hasSize(1);
        assertThat(bookService.getBookByGenre("Sci-Fi")).hasSize(1);
        assertThat(bookService.getBookByPages(123)).hasSize(1);
        assertThat(bookService.getBookByPrice(15.5)).hasSize(1);
        assertThat(bookService.getBookByAvailability(true)).hasSize(1);

        verify(bookRepository).findByTitle("Duna");
        verify(bookRepository).findByGenre("Sci-Fi");
        verify(bookRepository).findByPages(123);
        verify(bookRepository).findByPrice(15.5);
        verify(bookRepository).findByAvailable(true);
        verifyNoMoreInteractions(bookRepository);
    }

    // ===================== CREATE =====================

    @Test
    @DisplayName("saveBook guarda y devuelve el libro con ID asignado")
    void saveBook_persists() {
        Book toSave = b(0, "Nuevo", "Narrativa", 120, 7.5, true);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book arg = inv.getArgument(0);
            arg.setId(42L);
            return arg;
        });

        Book saved = bookService.saveBook(toSave);

        assertThat(saved.getId()).isEqualTo(42L);
        assertThat(saved.getTitle()).isEqualTo("Nuevo");

        verify(bookRepository).save(toSave);
        verifyNoMoreInteractions(bookRepository);
    }

    // ===================== UPDATE (PUT) =====================

    @Test
    @DisplayName("updateBook actualiza si existe")
    void updateBook_success() throws BookNotFoundException {
        Book existing = b(10, "Viejo", "Drama", 100, 5.0, false);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book changes = b(0, "Nuevo título", "Terror", 222, 13.37, true);

        Book updated = bookService.updateBook(10L, changes);

        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getTitle()).isEqualTo("Nuevo título");
        assertThat(updated.getGenre()).isEqualTo("Terror");
        assertThat(updated.getPages()).isEqualTo(222);
        assertThat(updated.getPrice()).isEqualTo(13.37);
        assertThat(updated.isAvailable()).isTrue();

        verify(bookRepository).findById(10L);
        verify(bookRepository).save(existing);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("updateBook lanza excepción si no existe")
    void updateBook_notFound() {
        when(bookRepository.findById(555L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.updateBook(555L, b(0, "x", "y", 1, 1.0, true)));

        verify(bookRepository).findById(555L);
        verifyNoMoreInteractions(bookRepository);
    }

    // ===================== PATCH =====================

    @Test
    @DisplayName("updateBookPartial aplica cambios")
    void updateBookPartial_success() {
        Book existing = b(20, "Antiguo", "Ensayo", 90, 4.2, false);
        when(bookRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Antiguo pero dorado");
        updates.put("genre", "Clásico");
        updates.put("pages", 123);
        updates.put("price", 8.75);
        updates.put("available", true);

        Book patched = bookService.updateBookPartial(20L, updates);

        assertThat(patched.getTitle()).isEqualTo("Antiguo pero dorado");
        assertThat(patched.getGenre()).isEqualTo("Clásico");
        assertThat(patched.getPages()).isEqualTo(123);
        assertThat(patched.getPrice()).isEqualTo(8.75);
        assertThat(patched.isAvailable()).isTrue();

        verify(bookRepository).findById(20L);
        verify(bookRepository).save(existing);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("updateBookPartial ignora campos desconocidos")
    void updateBookPartial_ignoresUnknownField() {
        Book existing = b(21, "Algo", "Otro", 10, 1.0, true);
        when(bookRepository.findById(21L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updates = Map.of("noExiste", 123);

        Book patched = bookService.updateBookPartial(21L, updates);

        assertThat(patched.getTitle()).isEqualTo("Algo");

        verify(bookRepository).findById(21L);
        verify(bookRepository).save(existing);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("updateBookPartial lanza excepción si no existe")
    void updateBookPartial_notFound() {
        when(bookRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> bookService.updateBookPartial(404L, Map.of("title", "x")));

        verify(bookRepository).findById(404L);
        verifyNoMoreInteractions(bookRepository);
    }

    // ===================== DELETE =====================

    @Test
    @DisplayName("deleteBook elimina si existe")
    void deleteBook_success() throws BookNotFoundException {
        when(bookRepository.existsById(30L)).thenReturn(true);

        bookService.deleteBook(30L);

        verify(bookRepository).existsById(30L);
        verify(bookRepository).deleteById(30L);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("deleteBook lanza excepción si no existe")
    void deleteBook_notFound() {
        when(bookRepository.existsById(31L)).thenReturn(false);

        assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(31L));

        verify(bookRepository).existsById(31L);
        verify(bookRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(bookRepository);
    }

    // ===================== QUERIES =====================

    @Test
    @DisplayName("findBooksWithPagesGreaterThan delega en JPQL")
    void pagesGreaterThan_delegates() {
        when(bookRepository.findBooksWithPagesGreaterThan(300))
                .thenReturn(List.of(b(1, "Largo", "X", 350, 10.0, true)));

        assertThat(bookService.findBooksWithPagesGreaterThan(300)).hasSize(1);

        verify(bookRepository).findBooksWithPagesGreaterThan(300);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("findBooksWithPriceLessThan delega en JPQL")
    void priceLessThan_delegates() {
        when(bookRepository.findBooksWithPriceLessThan(15.5f))
                .thenReturn(List.of(b(2, "Barato", "Y", 100, 10.0, true)));

        assertThat(bookService.findBooksWithPriceLessThan(15.5f)).hasSize(1);

        verify(bookRepository).findBooksWithPriceLessThan(15.5f);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("findBooksWithGenreLike delega en JPQL")
    void genreLike_delegates() {
        when(bookRepository.findBooksWithGenreLike("sci"))
                .thenReturn(List.of(b(3, "Algo", "Sci-Fi", 200, 12.0, true)));

        assertThat(bookService.findBooksWithGenreLike("sci")).hasSize(1);

        verify(bookRepository).findBooksWithGenreLike("sci");
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("findBooksByTitleAndAvailableAndGenre delega en JPQL combinada")
    void titleAvailableGenre_delegates() {
        when(bookRepository.findBooksByTitleAndAvailableAndGenre("It", true, "Terror"))
                .thenReturn(List.of(b(4, "It", "Terror", 1100, 15.5, true)));

        assertThat(bookService.findBooksByTitleAndAvailableAndGenre("It", true, "Terror")).hasSize(1);

        verify(bookRepository).findBooksByTitleAndAvailableAndGenre("It", true, "Terror");
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("findBooksWithPriceGreaterThanNative delega en SQL nativa")
    void nativePriceGreaterThan_delegates() {
        when(bookRepository.findBooksWithPriceGreaterThanNative(20.0f))
                .thenReturn(List.of(b(5, "Caro", "Arte", 120, 30.0, true)));

        assertThat(bookService.findBooksWithPriceGreaterThanNative(20.0f)).hasSize(1);

        verify(bookRepository).findBooksWithPriceGreaterThanNative(20.0f);
        verifyNoMoreInteractions(bookRepository);
    }
}
