package com.svalero.Api_Library;

import com.svalero.Api_Library.DTO.LoanDTO;
import com.svalero.Api_Library.domain.Book;
import com.svalero.Api_Library.domain.Loan;
import com.svalero.Api_Library.exception.LoanNotFoundException;
import com.svalero.Api_Library.repository.LoanRepository;
import com.svalero.Api_Library.service.LoanService;
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
class LoanServiceTests {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanService loanService;

    // Helpers muy humildes, como nosotros
    private Loan loan(long id) {
        Loan l = new Loan();
        l.setId(id);
        l.setName("Prestamo X");
        l.setCustomerName("Alice");
        l.setEmail("alice@example.com");
        l.setLoanDate(LocalDate.of(2024, 1, 15));
        l.setQuantity(2);

        Book b = new Book();
        b.setId(10L);
        b.setTitle("Dune");
        l.setBook(b);
        return l;
    }

    private Loan loanNoBook(long id) {
        Loan l = loan(id);
        l.setBook(null);
        return l;
    }

    // ===================== READ =====================

    @Test
    @DisplayName("getAllLoans devuelve todos los préstamos")
    void getAllLoans_ok() {
        when(loanRepository.findAll()).thenReturn(List.of(loan(1), loan(2)));

        var result = loanService.getAllLoans();

        assertThat(result).hasSize(2);
        verify(loanRepository).findAll();
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("getLoanById devuelve el préstamo cuando existe")
    void getById_found() throws LoanNotFoundException {
        when(loanRepository.findById(7L)).thenReturn(Optional.of(loan(7)));

        var result = loanService.getLoanById(7L);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getCustomerName()).isEqualTo("Alice");
        verify(loanRepository).findById(7L);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("getLoanById lanza LoanNotFoundException cuando NO existe")
    void getById_notFound() {
        when(loanRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> loanService.getLoanById(404L));
        verify(loanRepository).findById(404L);
        verifyNoMoreInteractions(loanRepository);
    }

    // ===================== CREATE =====================

    @Test
    @DisplayName("saveLoan guarda y devuelve el préstamo")
    void save_ok() {
        Loan toSave = loan(0);
        when(loanRepository.save(toSave)).thenAnswer(inv -> {
            Loan saved = inv.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        var saved = loanService.saveLoan(toSave);

        assertThat(saved.getId()).isEqualTo(42L);
        assertThat(saved.getName()).isEqualTo("Prestamo X");
        verify(loanRepository).save(toSave);
        verifyNoMoreInteractions(loanRepository);
    }

    // ===================== DELETE =====================

    @Test
    @DisplayName("deleteLoan elimina cuando existe")
    void delete_ok() throws LoanNotFoundException {
        when(loanRepository.existsById(5L)).thenReturn(true);

        loanService.deleteLoan(5L);

        verify(loanRepository).existsById(5L);
        verify(loanRepository).deleteById(5L);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("deleteLoan lanza LoanNotFoundException cuando NO existe")
    void delete_notFound() {
        when(loanRepository.existsById(6L)).thenReturn(false);

        assertThrows(LoanNotFoundException.class, () -> loanService.deleteLoan(6L));
        verify(loanRepository).existsById(6L);
        verify(loanRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(loanRepository);
    }

    // ===================== UPDATE (PUT) =====================

    @Test
    @DisplayName("updateLoan copia campos y guarda cuando existe")
    void update_ok() throws LoanNotFoundException {
        Loan existing = loan(1);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan changes = new Loan();
        changes.setName("Nuevo Nombre");
        changes.setCustomerName("Bob");
        changes.setEmail("bob@example.com");
        changes.setLoanDate(LocalDate.of(2025, 2, 2));
        changes.setQuantity(9);
        Book newBook = new Book();
        newBook.setId(11L);
        newBook.setTitle("Foundation");
        changes.setBook(newBook);

        var updated = loanService.updateLoan(1L, changes);

        assertThat(updated.getName()).isEqualTo("Nuevo Nombre");
        assertThat(updated.getCustomerName()).isEqualTo("Bob");
        assertThat(updated.getEmail()).isEqualTo("bob@example.com");
        assertThat(updated.getLoanDate()).isEqualTo(LocalDate.of(2025, 2, 2));
        assertThat(updated.getQuantity()).isEqualTo(9);
        assertThat(updated.getBook().getId()).isEqualTo(11L);

        verify(loanRepository).findById(1L);
        verify(loanRepository).save(existing);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("updateLoan lanza LoanNotFoundException si no existe")
    void update_notFound() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> loanService.updateLoan(99L, loan(0)));
        verify(loanRepository).findById(99L);
        verifyNoMoreInteractions(loanRepository);
    }

    // ===================== UPDATE (PATCH) =====================

    @Test
    @DisplayName("updateLoanPartial convierte Strings a LocalDate/int y actualiza campos simples")
    void patch_conversions_ok() throws LoanNotFoundException {
        Loan existing = loan(2);
        when(loanRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updates = Map.of(
                "loanDate", "2025-03-10",   // String → LocalDate
                "quantity", "7",            // String → int
                "name", "PatchName"         // simple String
        );

        var patched = loanService.updateLoanPartial(2L, updates);

        assertThat(patched.getLoanDate()).isEqualTo(LocalDate.of(2025, 3, 10));
        assertThat(patched.getQuantity()).isEqualTo(7);
        assertThat(patched.getName()).isEqualTo("PatchName");

        verify(loanRepository).findById(2L);
        verify(loanRepository).save(existing);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("updateLoanPartial con Number también actualiza quantity")
    void patch_number_ok() throws LoanNotFoundException {
        Loan existing = loan(3);
        when(loanRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updates = Map.of("quantity", 15);

        var patched = loanService.updateLoanPartial(3L, updates);

        assertThat(patched.getQuantity()).isEqualTo(15);
        verify(loanRepository).findById(3L);
        verify(loanRepository).save(existing);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("updateLoanPartial lanza LoanNotFoundException cuando no existe")
    void patch_notFound() {
        when(loanRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> loanService.updateLoanPartial(404L, Map.of("name", "x")));
        verify(loanRepository).findById(404L);
        verifyNoMoreInteractions(loanRepository);
    }

    // ===================== QUERIES PERSONALIZADAS =====================

    @Test
    @DisplayName("getLoanByCustomerName delega en repositorio")
    void byCustomerName_delegates() {
        when(loanRepository.findByCustomerName("Alice")).thenReturn(List.of(loan(1)));

        var result = loanService.getLoanByCustomerName("Alice");

        assertThat(result).hasSize(1);
        verify(loanRepository).findByCustomerName("Alice");
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("getLoanByLoanDate delega en repositorio")
    void byLoanDate_delegates() {
        LocalDate d = LocalDate.of(2024, 1, 15);
        when(loanRepository.findByLoanDate(d)).thenReturn(List.of(loan(2)));

        var result = loanService.getLoanByLoanDate(d);

        assertThat(result).hasSize(1);
        verify(loanRepository).findByLoanDate(d);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("getLoansBetweenDates delega en repositorio")
    void betweenDates_delegates() {
        LocalDate a = LocalDate.of(2024, 1, 1);
        LocalDate b = LocalDate.of(2024, 12, 31);
        when(loanRepository.findByLoanDateBetween(a, b)).thenReturn(List.of(loan(3)));

        var result = loanService.getLoansBetweenDates(a, b);

        assertThat(result).hasSize(1);
        verify(loanRepository).findByLoanDateBetween(a, b);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("getLoanByQuantity delega en repositorio")
    void byQuantity_delegates() {
        when(loanRepository.findByQuantity(2)).thenReturn(List.of(loan(4)));

        var result = loanService.getLoanByQuantity(2);

        assertThat(result).hasSize(1);
        verify(loanRepository).findByQuantity(2);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("findLoansWithQuantityGreaterThanNative delega en nativa")
    void nativeGreaterThan_delegates() {
        when(loanRepository.findLoansWithQuantityGreaterThanNative(5)).thenReturn(List.of(loan(5)));

        var result = loanService.findLoansWithQuantityGreaterThanNative(5);

        assertThat(result).hasSize(1);
        verify(loanRepository).findLoansWithQuantityGreaterThanNative(5);
        verifyNoMoreInteractions(loanRepository);
    }

    // ===================== DTO =====================

    @Test
    @DisplayName("convertToDTO incluye book cuando existe")
    void dto_with_book() {
        Loan l = loan(8);

        LoanDTO dto = loanService.convertToDTO(l);

        assertThat(dto.getId()).isEqualTo(8L);
        assertThat(dto.getName()).isEqualTo("Prestamo X");
        assertThat(dto.getCustomerName()).isEqualTo("Alice");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getLoanDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(dto.getQuantity()).isEqualTo(2);
        assertThat(dto.getBook()).isNotNull();
        assertThat(dto.getBook().getId()).isEqualTo(10L);
        assertThat(dto.getBook().getTitle()).isEqualTo("Dune");
    }

    @Test
    @DisplayName("convertToDTO pone book a null cuando no hay relación")
    void dto_without_book() {
        Loan l = loanNoBook(9);

        LoanDTO dto = loanService.convertToDTO(l);

        assertThat(dto.getBook()).isNull();
    }
}
