package com.svalero.Api_Library.controller;

import com.svalero.Api_Library.DTO.LoanDTO;
import com.svalero.Api_Library.domain.Loan;
import com.svalero.Api_Library.exception.LoanNotFoundException;
import com.svalero.Api_Library.service.LoanService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final Logger logger = LoggerFactory.getLogger(LoanController.class);
    private final LoanService service;

    @Autowired
    public LoanController(LoanService service) {
        this.service = service;
    }

    // GET: Listar todos los préstamos
    @GetMapping
    public List<LoanDTO> getAllLoans() {
        logger.info("Fetching all loans");
        return service.getAllLoans()
                .stream().map(service::convertToDTO)
                .collect(Collectors.toList());
    }

    // GET: Buscar préstamo por ID
    @GetMapping("/{id}")
    public ResponseEntity<LoanDTO> getLoanById(@PathVariable long id) throws LoanNotFoundException {
        logger.info("Fetching loan by ID: {}", id);
        Loan loan = service.getLoanById(id);
        return ResponseEntity.ok(service.convertToDTO(loan));
    }

    // GET: Buscar préstamos por nombre de cliente
    @GetMapping("/customer-name")
    public ResponseEntity<List<LoanDTO>> getByCustomerName(@RequestParam String customerName) {
        logger.info("Fetching loans for customer: {}", customerName);
        List<LoanDTO> loanDTOs = service.getLoanByCustomerName(customerName)
                .stream().map(service::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDTOs);
    }

    // GET: Buscar préstamos por fecha (ISO: yyyy-MM-dd)
    @GetMapping("/loan-date")
    public ResponseEntity<List<LoanDTO>> getByLoanDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate loanDate) {
        logger.info("Fetching loans for loan date: {}", loanDate);
        List<LoanDTO> loanDTOs = service.getLoanByLoanDate(loanDate)
                .stream().map(service::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDTOs);
    }

    // GET: Buscar préstamos entre dos fechas (ISO)
    @GetMapping("/range")
    public ResponseEntity<List<LoanDTO>> getLoansBetweenDates(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.info("Fetching loans from {} to {}", startDate, endDate);
        List<LoanDTO> loanDTOs = service.getLoansBetweenDates(startDate, endDate)
                .stream().map(service::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDTOs);
    }

    // === CANTIDAD EXACTA (NO NATIVA) -> /loans/quantity/eq/{quantity}
    @GetMapping("/quantity/eq/{quantity}")
    public ResponseEntity<List<LoanDTO>> getByQuantity(@PathVariable int quantity) {
        logger.info("Fetching loans by exact quantity: {}", quantity);
        List<LoanDTO> loanDTOs = service.getLoanByQuantity(quantity)
                .stream().map(service::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDTOs);
    }

    // === CANTIDAD MAYOR QUE (SQL NATIVA) -> /loans/quantity/native/gt/{min}
    @GetMapping("/quantity/native/gt/{min}")
    public ResponseEntity<List<LoanDTO>> getLoansWithQuantityGreaterThanNative(@PathVariable int min) {
        logger.info("Fetching loans with quantity > {} (native)", min);
        List<LoanDTO> loanDTOs = service.findLoansWithQuantityGreaterThanNative(min)
                .stream().map(service::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDTOs);
    }

    // POST: Crear nuevo préstamo
    @PostMapping
    public ResponseEntity<LoanDTO> addLoan(@Valid @RequestBody Loan loan) {
        logger.info("Adding loan for customer: {}", loan.getCustomerName());
        Loan newLoan = service.saveLoan(loan);
        return new ResponseEntity<>(service.convertToDTO(newLoan), HttpStatus.CREATED);
    }

    // PUT: Actualizar préstamo completo
    @PutMapping("/{id}")
    public ResponseEntity<LoanDTO> updateLoan(
            @PathVariable long id,
            @Valid @RequestBody Loan loanDetails) throws LoanNotFoundException {
        logger.info("Updating loan ID: {}", id);
        Loan updatedLoan = service.updateLoan(id, loanDetails);
        return ResponseEntity.ok(service.convertToDTO(updatedLoan));
    }

    // PATCH: Actualización parcial
    @PatchMapping("/{id}")
    public ResponseEntity<LoanDTO> updateLoanPartial(
            @PathVariable long id,
            @RequestBody Map<String, Object> updates) {
        logger.info("Partially updating loan ID: {}", id);
        Loan updatedLoan = service.updateLoanPartial(id, updates);
        return ResponseEntity.ok(service.convertToDTO(updatedLoan));
    }

    // DELETE: Eliminar préstamo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable long id) throws LoanNotFoundException {
        logger.info("Deleting loan ID: {}", id);
        service.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }
}
