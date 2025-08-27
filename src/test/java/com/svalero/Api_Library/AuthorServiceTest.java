package com.svalero.Api_Library;

import com.svalero.Api_Library.domain.Author;
import com.svalero.Api_Library.exception.AuthorNotFoundException;
import com.svalero.Api_Library.repository.AuthorRepository;
import com.svalero.Api_Library.service.AuthorService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTests {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author sampleAuthor() {
        // Orden EXACTO según tu entidad:
        // id, name, surname, birthdate, active, nationality, latitude, longitude, books
        return new Author(
                1L,
                "Isaac",
                "Asimov",
                LocalDate.of(1920, 1, 2),
                true,
                "Russian-American",
                40.0,
                -3.0,
                null
        );
    }

    // ================== GET ALL ==================
    @Test
    @DisplayName("getAllAuthors devuelve lista completa")
    void getAllAuthors_ok() {
        when(authorRepository.findAll()).thenReturn(List.of(sampleAuthor()));

        var result = authorService.getAllAuthors();

        assertThat(result).hasSize(1);
        verify(authorRepository).findAll();
        verifyNoMoreInteractions(authorRepository);
    }

    // ================== GET BY FILTERS ==================
    @Test
    @DisplayName("getAuthorByName devuelve coincidencias")
    void getByName_ok() {
        when(authorRepository.findByName("Isaac")).thenReturn(List.of(sampleAuthor()));

        var result = authorService.getAuthorByName("Isaac");

        assertThat(result).extracting(Author::getName).contains("Isaac");
        verify(authorRepository).findByName("Isaac");
        verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName("getAuthorBySurname devuelve coincidencias")
    void getBySurname_ok() {
        when(authorRepository.findBySurname("Asimov")).thenReturn(List.of(sampleAuthor()));

        var result = authorService.getAuthorBySurname("Asimov");

        assertThat(result).extracting(Author::getSurname).contains("Asimov");
        verify(authorRepository).findBySurname("Asimov");
        verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName("getAuthorByNationality devuelve coincidencias")
    void getByNationality_ok() {
        when(authorRepository.findByNationality("Russian-American")).thenReturn(List.of(sampleAuthor()));

        var result = authorService.getAuthorByNationality("Russian-American");

        assertThat(result).extracting(Author::getNationality).contains("Russian-American");
        verify(authorRepository).findByNationality("Russian-American");
        verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName("getAuthorByBirthdate devuelve coincidencias")
    void getByBirthdate_ok() {
        LocalDate date = LocalDate.of(1920, 1, 2);
        when(authorRepository.findByBirthdate(date)).thenReturn(List.of(sampleAuthor()));

        var result = authorService.getAuthorByBirthdate(date);

        assertThat(result).hasSize(1);
        verify(authorRepository).findByBirthdate(date);
        verifyNoMoreInteractions(authorRepository);
    }

    // ================== GET BY ID ==================
    @Test
    @DisplayName("getAuthorById devuelve autor existente")
    void getById_found() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(sampleAuthor()));

        var author = authorService.getAuthorById(1L);

        assertThat(author.getId()).isEqualTo(1L);
        verify(authorRepository).findById(1L);
        verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName("getAuthorById lanza RuntimeException si no existe (como en tu servicio)")
    void getById_notFound() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authorService.getAuthorById(99L));
        verify(authorRepository).findById(99L);
        verifyNoMoreInteractions(authorRepository);
    }

    // ================== SAVE ==================
    @Test
    @DisplayName("saveAuthor guarda y devuelve autor")
    void save_ok() {
        var author = sampleAuthor();
        when(authorRepository.save(author)).thenReturn(author);

        var saved = authorService.saveAuthor(author);

        assertThat(saved.getName()).isEqualTo("Isaac");
        verify(authorRepository).save(author);
        verifyNoMoreInteractions(authorRepository);
    }

    // ================== DELETE ==================
    @Test
    @DisplayName("deleteAuthor borra si existe")
    void delete_ok() throws AuthorNotFoundException {
        when(authorRepository.existsById(1L)).thenReturn(true);

        authorService.deleteAuthor(1L);

        verify(authorRepository).existsById(1L);
        verify(authorRepository).deleteById(1L);
        verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName("deleteAuthor lanza excepción si no existe")
    void delete_notFound() {
        when(authorRepository.existsById(99L)).thenReturn(false);

        assertThrows(AuthorNotFoundException.class, () -> authorService.deleteAuthor(99L));
        verify(authorRepository).existsById(99L);
        verify(authorRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(authorRepository);
    }

    // ================== UPDATE (PUT) ==================
    @Test
    @DisplayName("updateAuthor actualiza campos si existe")
    void update_ok() throws AuthorNotFoundException {
        var existing = sampleAuthor();
        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));

        // Orden y tipos correctos
        var changes = new Author(
                0L,
                "Nuevo",
                "Apellido",
                LocalDate.of(2000, 1, 1),
                false,
                "Spanish",
                41.0,
                -4.0,
                null
        );

        var updated = authorService.updateAuthor(1L, changes);

        assertThat(updated.getName()).isEqualTo("Nuevo");
        assertThat(updated.getSurname()).isEqualTo("Apellido");
        assertThat(updated.getBirthdate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(updated.getActive()).isFalse();
        assertThat(updated.getNationality()).isEqualTo("Spanish");
        assertThat(updated.getLatitude()).isEqualTo(41.0);
        assertThat(updated.getLongitude()).isEqualTo(-4.0);

        verify(authorRepository).findById(1L);
        verify(authorRepository).save(existing);
        verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName("updateAuthor lanza excepción si no existe")
    void update_notFound() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class, () -> authorService.updateAuthor(99L, sampleAuthor()));
        verify(authorRepository).findById(99L);
        verifyNoMoreInteractions(authorRepository);
    }

    // ================== UPDATE (PATCH) ==================
    @Test
    @DisplayName("updateAuthorPartial actualiza solo campos dados (reflexión)")
    void patch_ok() {
        var existing = sampleAuthor();
        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));

        var patched = authorService.updateAuthorPartial(1L, Map.of(
                "name", "NombreNuevo",
                "active", false
        ));

        assertThat(patched.getName()).isEqualTo("NombreNuevo");
        assertThat(patched.getActive()).isFalse();

        verify(authorRepository).findById(1L);
        verify(authorRepository).save(existing);
        verifyNoMoreInteractions(authorRepository);
    }
}
