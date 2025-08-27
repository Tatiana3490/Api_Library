package com.svalero.Api_Library;

import com.svalero.Api_Library.domain.BookCategory;
import com.svalero.Api_Library.exception.BookCategoryNotFoundException;
import com.svalero.Api_Library.repository.BookCategoryRepository;
import com.svalero.Api_Library.service.BookCategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de BookCategoryService.
 * - No arranca Spring, no toca BD.
 * - Patrón: Arrange / Act / Assert + verify del repo.
 */
@ExtendWith(MockitoExtension.class)
class BookCategoryServiceTests {

    @Mock
    private BookCategoryRepository bookCategoryRepository;

    @InjectMocks
    private BookCategoryService bookCategoryService;

    // Helper: creo categorías rápido para no escribir 7 setters en cada test
    private BookCategory cat(
            long id, String name, String desc, boolean active, LocalDate created, int numberBooks
    ) {
        BookCategory c = new BookCategory();
        c.setId(id);
        c.setName(name);
        c.setDescription(desc);
        c.setActive(active);
        c.setCreatedDate(created);
        c.setNumberBooks(numberBooks);
        return c;
    }

    // ================= READ =================

    @Test
    @DisplayName("getAllBookCategories devuelve todas las categorías (delegando en findAll)")
    void getAll_returnsAll() {
        when(bookCategoryRepository.findAll()).thenReturn(List.of(
                cat(1, "Sci-Fi", "Naves y rayos", true, LocalDate.of(2020,1,1), 50),
                cat(2, "Drama", "Lloros elegantes", true, LocalDate.of(2021,5,10), 30)
        ));

        var result = bookCategoryService.getAllBookCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Sci-Fi");

        verify(bookCategoryRepository).findAll();
        verifyNoMoreInteractions(bookCategoryRepository);
    }

    @Test
    @DisplayName("getBookCategoriesById devuelve la categoría cuando existe")
    void getById_found() {
        when(bookCategoryRepository.findById(7L))
                .thenReturn(Optional.of(cat(7, "Poesía", "Rimas y tal", true, LocalDate.now(), 12)));

        var result = bookCategoryService.getBookCategoriesById(7L);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getName()).isEqualTo("Poesía");

        verify(bookCategoryRepository).findById(7L);
        verifyNoMoreInteractions(bookCategoryRepository);
    }

    @Test
    @DisplayName("getBookCategoriesById lanza RuntimeException cuando NO existe (tal y como está el servicio ahora)")
    void getById_notFound_runtime() {
        when(bookCategoryRepository.findById(404L)).thenReturn(Optional.empty());

        // Tu service ahora lanza RuntimeException (no BookCategoryNotFoundException) aquí
        assertThrows(RuntimeException.class, () -> bookCategoryService.getBookCategoriesById(404L));

        verify(bookCategoryRepository).findById(404L);
        verifyNoMoreInteractions(bookCategoryRepository);
    }

    @Test
    @DisplayName("Filtros simples delegan en el método correcto del repo (name/description/active/createdDate/numberBooks)")
    void simpleFilters_delegate() {
        when(bookCategoryRepository.findByName("Sci-Fi"))
                .thenReturn(List.of(cat(1,"Sci-Fi","X", true, LocalDate.now(), 10)));
        when(bookCategoryRepository.findByDescription("Oscura"))
                .thenReturn(List.of(cat(2,"Terror","Oscura", true, LocalDate.now(), 5)));
        when(bookCategoryRepository.findByActive(true))
                .thenReturn(List.of(cat(3,"Drama","Y", true, LocalDate.now(), 8)));
        when(bookCategoryRepository.findByCreatedDate(LocalDate.of(2024,1,1)))
                .thenReturn(List.of(cat(4,"Nuevo","Z", false, LocalDate.of(2024,1,1), 1)));
        when(bookCategoryRepository.findByNumberBooks(42))
                .thenReturn(List.of(cat(5,"Raros","K", true, LocalDate.now(), 42)));

        assertThat(bookCategoryService.getBookCategoriesByName("Sci-Fi")).hasSize(1);
        assertThat(bookCategoryService.getBookCategoriesByDescription("Oscura")).hasSize(1);
        assertThat(bookCategoryService.getBookCategoriesByActive(true)).hasSize(1);
        assertThat(bookCategoryService.getBookCategoriesByCreateDate(LocalDate.of(2024,1,1))).hasSize(1);
        assertThat(bookCategoryService.getBookCategoriesByNumberBooks(42)).hasSize(1);

        verify(bookCategoryRepository).findByName("Sci-Fi");
        verify(bookCategoryRepository).findByDescription("Oscura");
        verify(bookCategoryRepository).findByActive(true);
        verify(bookCategoryRepository).findByCreatedDate(LocalDate.of(2024,1,1));
        verify(bookCategoryRepository).findByNumberBooks(42);
        verifyNoMoreInteractions(bookCategoryRepository);
    }

    @Test
    @DisplayName("getBookCategoriesWithMinBooks delega en findByNumberBooksGreaterThan")
    void minBooks_delegate() {
        when(bookCategoryRepository.findByNumberBooksGreaterThan(10))
                .thenReturn(List.of(cat(1,"A","", true, LocalDate.now(), 11)));

        assertThat(bookCategoryService.getBookCategoriesWithMinBooks(10)).hasSize(1);

        verify(bookCategoryRepository).findByNumberBooksGreaterThan(10);
        verifyNoMoreInteractions(bookCategoryRepository);
    }

    // ================= CREATE =================

    @Test
    @DisplayName("saveBookCategory guarda y devuelve la categoría")
    void save_persists() {
        when(bookCategoryRepository.save(any(BookCategory.class))).thenAnswer(inv -> {
            BookCategory bc = inv.getArgument(0);
            bc.setId(99L); // simula ID de BD
            return bc;
        });

        var saved = bookCategoryService.saveBookCategory(cat(0,"Nueva","Desc", true, LocalDate.now(), 3));

        assertThat(saved.getId()).isEqualTo(99L);
        assertThat(saved.getName()).isEqualTo("Nueva");

        verify(bookCategoryRepository).save(any(BookCategory.class));
        verifyNoMoreInteractions(bookCategoryRepository);
    }

    // ================= UPDATE (PUT) =================

    @Test
    @DisplayName("updateBookCategory actualiza campos y guarda cuando existe")
    void update_success() {
        var existing = cat(10,"Vieja","D1", false, LocalDate.of(2020,1,1), 5);
        when(bookCategoryRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(bookCategoryRepository.save(any(BookCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        var changes = cat(0,"Moderna","D2", true, LocalDate.of(2024,6,6), 42);

        var updated = bookCategoryService.updateBookCategory(10L, changes);

        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getName()).isEqualTo("Moderna");
        assertThat(updated.getDescription()).isEqualTo("D2");
        assertThat(updated.getActive()).isTrue();
        assertThat(updated.getCreatedDate()).isEqualTo(LocalDate.of(2024,6,6));
        assertThat(updated.getNumberBooks()).isEqualTo(42);

        verify(bookCategoryRepository).findById(10L);
        verify(bookCategoryRepository).save(existing);
        verifyNoMoreInteractions(bookCategoryRepository);
    }

    @Test
    @DisplayName("updateBookCategory lanza RuntimeException cuando NO existe (tal y como está el servicio)")
    void update_notFound_runtime() {
        when(bookCategoryRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> bookCategoryService.updateBookCategory(123L, cat(0,"X","Y", true, LocalDate.now(), 0)));

        verify(bookCategoryRepository).findById(123L);
        verifyNoMoreInteractions(bookCategoryRepository);
    }

    // ================= PATCH =================


    // TIP: cuando arregles el bug (ver notas abajo), este test sería el correcto:
     @Test
     @DisplayName("updateBookCategoryPartial aplica cambios con reflexión y guarda (tras fix)")
     void patch_success_after_fix() {
         var existing = cat(21,"Old","Desc", false, LocalDate.of(2022,2,2), 7);
         when(bookCategoryRepository.findById(21L)).thenReturn(Optional.of(existing));
         when(bookCategoryRepository.save(any(BookCategory.class))).thenAnswer(inv -> inv.getArgument(0));

         Map<String, Object> updates = Map.of(
                 "name", "NewName",
                 "description", "NewDesc",
                 "active", true,
                 "numberBooks", 99
         );

         var patched = bookCategoryService.updateBookCategoryPartial(21L, updates);

         assertThat(patched.getName()).isEqualTo("NewName");
         assertThat(patched.getDescription()).isEqualTo("NewDesc");
         assertThat(patched.getActive()).isTrue();
         assertThat(patched.getNumberBooks()).isEqualTo(99);

         verify(bookCategoryRepository).findById(21L);
         verify(bookCategoryRepository).save(existing);
         verifyNoMoreInteractions(bookCategoryRepository);
     }

    // ================= DELETE =================

    @Test
    @DisplayName("deleteBookCategory elimina cuando existe")
    void delete_success() throws BookCategoryNotFoundException {
        when(bookCategoryRepository.existsById(30L)).thenReturn(true);

        bookCategoryService.deleteBookCategory(30L);

        verify(bookCategoryRepository).existsById(30L);
        verify(bookCategoryRepository).deleteById(30L);
        verifyNoMoreInteractions(bookCategoryRepository);
    }

    @Test
    @DisplayName("deleteBookCategory lanza BookCategoryNotFoundException cuando NO existe")
    void delete_notFound() {
        when(bookCategoryRepository.existsById(31L)).thenReturn(false);

        assertThrows(BookCategoryNotFoundException.class,
                () -> bookCategoryService.deleteBookCategory(31L));

        verify(bookCategoryRepository).existsById(31L);
        verify(bookCategoryRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(bookCategoryRepository);
    }
}
