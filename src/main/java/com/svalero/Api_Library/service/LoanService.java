package com.svalero.Api_Library.service;

import com.svalero.Api_Library.DTO.BookDTO;
import com.svalero.Api_Library.DTO.LoanDTO;
import com.svalero.Api_Library.domain.Loan;
import com.svalero.Api_Library.exception.LoanNotFoundException;
import com.svalero.Api_Library.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class LoanService {

    private final LoanRepository loanRepository;

    @Autowired
    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    // --- CRUD BÁSICO ---

    // Obtener todos los préstamos
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    // Obtener un préstamo por ID
    public Loan getLoanById(long id) throws LoanNotFoundException {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + id));
    }

    // Guardar un nuevo préstamo
    public Loan saveLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    // Eliminar un préstamo por ID
    public void deleteLoan(long id) throws LoanNotFoundException {
        if (!loanRepository.existsById(id)) {
            throw new LoanNotFoundException("Loan not found with id: " + id);
        }
        loanRepository.deleteById(id);
    }

    // Actualizar un préstamo completo por ID
    public Loan updateLoan(long id, Loan loanDetails) throws LoanNotFoundException {
        Loan existingLoan = getLoanById(id); // ya lanza la excepción si no existe

        existingLoan.setName(loanDetails.getName());
        existingLoan.setCustomerName(loanDetails.getCustomerName());
        existingLoan.setEmail(loanDetails.getEmail());
        existingLoan.setLoanDate(loanDetails.getLoanDate());
        existingLoan.setQuantity(loanDetails.getQuantity());
        existingLoan.setBook(loanDetails.getBook());

        return loanRepository.save(existingLoan);
    }

    // Actualización parcial con Map de campos
    public Loan updateLoanPartial(long id, Map<String, Object> updates) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + id));

        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Loan.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, loan, value);
            }
        });

        return loanRepository.save(loan);
    }

    // --- BÚSQUEDAS PERSONALIZADAS ---

    public List<Loan> getLoanByCustomerName(String customerName) {
        return loanRepository.findByCustomerName(customerName);
    }

    public List<Loan> getLoanByLoanDate(LocalDate loanDate) {
        return loanRepository.findByLoanDate(loanDate);
    }

    public List<Loan> getLoansBetweenDates(LocalDate startDate, LocalDate endDate) {
        return loanRepository.findByLoanDateBetween(startDate, endDate);
    }

    public List<Loan> getLoanByQuantity(int quantity) {
        return loanRepository.findByQuantity(quantity);
    }

    // --- CONVERSIÓN A DTO (para no devolver JSON infinitos) ---

    public LoanDTO convertToDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();

        dto.setId(loan.getId());
        dto.setName(loan.getName());
        dto.setCustomerName(loan.getCustomerName());
        dto.setEmail(loan.getEmail());
        dto.setLoanDate(loan.getLoanDate());
        dto.setQuantity(loan.getQuantity());

        // DTO anidado para el libro relacionado
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(loan.getBook().getId());
        bookDTO.setTitle(loan.getBook().getTitle());

        dto.setBook(bookDTO);

        return dto;
    }
}
