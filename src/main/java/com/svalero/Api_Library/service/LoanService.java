package com.svalero.Api_Library.service;

import com.svalero.Api_Library.DTO.BookDTO;
import com.svalero.Api_Library.DTO.LoanDTO;
import com.svalero.Api_Library.domain.Loan;
import com.svalero.Api_Library.exception.LoanNotFoundException;
import com.svalero.Api_Library.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // =========================
    // CRUD BÁSICO
    // =========================

    /** Lista todos los préstamos. */
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    /** Devuelve un préstamo por ID o lanza excepción si no existe. */
    public Loan getLoanById(long id) throws LoanNotFoundException {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + id));
    }

    /** Crea/guarda un nuevo préstamo. */
    @Transactional //indica que un metodo o clase debe ejecutarse dentro de una transaccion de bdd.
    public Loan saveLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    /** Elimina un préstamo por ID, validando que exista. */
    @Transactional
    public void deleteLoan(long id) throws LoanNotFoundException {
        if (!loanRepository.existsById(id)) {
            throw new LoanNotFoundException("Loan not found with id: " + id);
        }
        loanRepository.deleteById(id);
    }

    /** Reemplaza por completo un préstamo existente (PUT). */
    @Transactional
    public Loan updateLoan(long id, Loan loanDetails) throws LoanNotFoundException {
        // Si no existe, esto lanza LoanNotFoundException
        Loan existingLoan = getLoanById(id);

        // 👇 "Soy dummie": aquí copio campo a campo lo que sí permito actualizar.
        existingLoan.setName(loanDetails.getName());
        existingLoan.setCustomerName(loanDetails.getCustomerName());
        existingLoan.setEmail(loanDetails.getEmail());
        existingLoan.setLoanDate(loanDetails.getLoanDate());
        existingLoan.setQuantity(loanDetails.getQuantity());
        existingLoan.setBook(loanDetails.getBook()); // OJO: se espera un Book válido con ID existente

        return loanRepository.save(existingLoan);
    }

    /** Actualización parcial (PATCH) con mapa de campos. */
    @Transactional
    public Loan updateLoanPartial(long id, Map<String, Object> updates) throws LoanNotFoundException {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + id));

        // 👇 "Soy dummie": si me pasan claves que coinciden con atributos de Loan, intento poner su valor.
        //      - Convierto tipos simples cuando hace falta (p.ej., String -> LocalDate).
        //      - Para relaciones (book) solo permito actualizar via bookId (más seguro).
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Caso especial 1: fecha (me puede llegar como String)
            if ("loanDate".equals(key) && value instanceof String dateStr) {
                loan.setLoanDate(LocalDate.parse(dateStr)); // Se espera formato ISO yyyy-MM-dd
                continue;
            }

            // Caso especial 2: quantity (podría venir como Integer o String)
            if ("quantity".equals(key)) {
                if (value instanceof Number n) {
                    loan.setQuantity(n.intValue());
                } else if (value instanceof String s) {
                    loan.setQuantity(Integer.parseInt(s));
                }
                continue;
            }

            // Caso especial 3: actualizar el book por su id (más seguro que pasar un objeto anidado)
            if ("bookId".equals(key)) {
                // Aquí lo ideal sería cargar Book por su id desde BookRepository (si lo tienes).
                // Como no tenemos BookRepository aquí, lo marco para que no rompa:
                // TODO: inyectar BookRepository y hacer: Book b = bookRepo.findById(...).orElseThrow(...)
                // loan.setBook(b);
                // Mientras tanto, si ya tienes el objeto Book cargado en otro sitio, ignora esto.
                continue;
            }

            // Para el resto de campos simples (name, customerName, email, etc.) tiro de reflexión
            Field field = ReflectionUtils.findField(Loan.class, key);
            if (field != null) {
                field.setAccessible(true);

                // 👇 Conversión muy básica para Strings numéricos → int/long si el field lo requiere
                Class<?> type = field.getType();
                Object coerced = value;

                // Conversión simple (solo si hace falta)
                try {
                    if (type.equals(Integer.class) || type.equals(int.class)) {
                        if (value instanceof String s) coerced = Integer.parseInt(s);
                    } else if (type.equals(Long.class) || type.equals(long.class)) {
                        if (value instanceof String s) coerced = Long.parseLong(s);
                    } else if (type.equals(LocalDate.class) && value instanceof String s) {
                        coerced = LocalDate.parse(s);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid value for field '" + key + "': " + value);
                }

                ReflectionUtils.setField(field, loan, coerced);
            }
        }

        return loanRepository.save(loan);
    }

    // =========================
    // BÚSQUEDAS PERSONALIZADAS
    // =========================

    /** Busca por nombre de cliente. */
    public List<Loan> getLoanByCustomerName(String customerName) {
        return loanRepository.findByCustomerName(customerName);
    }

    /** Busca por fecha exacta. */
    public List<Loan> getLoanByLoanDate(LocalDate loanDate) {
        return loanRepository.findByLoanDate(loanDate);
    }

    /** Busca entre dos fechas (inclusive). */
    public List<Loan> getLoansBetweenDates(LocalDate startDate, LocalDate endDate) {
        return loanRepository.findByLoanDateBetween(startDate, endDate);
    }

    /** Busca por cantidad EXACTA. */
    public List<Loan> getLoanByQuantity(int quantity) {
        return loanRepository.findByQuantity(quantity);
    }

    /** Busca por cantidad MAYOR QUE usando SQL nativa (coincide con el endpoint /quantity/native/gt/{min}). */
    public List<Loan> findLoansWithQuantityGreaterThanNative(int min) {
        return loanRepository.findLoansWithQuantityGreaterThanNative(min);
    }

    // =========================
    // CONVERSIÓN A DTO
    // =========================

    /**
     * Convierto Loan a LoanDTO para no devolver todo el objeto (evito ciclos y JSON enormes).
     * "Soy dummie": aquí solo saco lo que quiero exponer por API.
     */
    public LoanDTO convertToDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();

        dto.setId(loan.getId());
        dto.setName(loan.getName());
        dto.setCustomerName(loan.getCustomerName());
        dto.setEmail(loan.getEmail());
        dto.setLoanDate(loan.getLoanDate());
        dto.setQuantity(loan.getQuantity());

        // DTO anidado para el libro relacionado (null-safe por si no hay libro asignado)
        if (loan.getBook() != null) {
            BookDTO bookDTO = new BookDTO();
            bookDTO.setId(loan.getBook().getId());
            bookDTO.setTitle(loan.getBook().getTitle());
            dto.setBook(bookDTO);
        } else {
            dto.setBook(null);
        }

        return dto;
    }
}
