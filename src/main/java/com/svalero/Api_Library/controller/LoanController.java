package com.svalero.Api_Library.controller;

import com.svalero.Api_Library.domain.Loan;
import com.svalero.Api_Library.exception.LoanNotFoundException;
import com.svalero.Api_Library.service.LoanService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<Loan>> getAllLoans() {
        logger.info("Fetching all loans");
        List<Loan> loans = service.getAllLoans();
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    // GET: Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable long id) throws LoanNotFoundException {
        logger.info("Fetching loan by ID: {}", id);
        Loan loan = service.getLoanById(id);
        return new ResponseEntity<>(loan, HttpStatus.OK);
    }

    // GET: Buscar por nombre de cliente
    @GetMapping("/customer-name")
    public ResponseEntity<List<Loan>> getByCustomerName(@RequestParam String customerName) {
        logger.info("Fetching loans for customer: {}", customerName);
        List<Loan> loans = service.getLoanByCustomerName(customerName);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    // GET: Buscar por fecha de préstamo
    @GetMapping("/loan-date")
    public ResponseEntity<List<Loan>> getByLoanDate(@RequestParam LocalDate loanDate) {
        logger.info("Fetching loans for loan date: {}", loanDate);
        List<Loan> loans = service.getLoanByLoanDate(loanDate);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    // GET: Buscar por rango de fechas
    @GetMapping("/range")
    public ResponseEntity<List<Loan>> getByDateRange(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        logger.info("Fetching loans between {} and {}", startDate, endDate);
        List<Loan> loans = service.getLoansBetweenDates(startDate, endDate);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    // GET: Buscar por cantidad de libros prestados
    @GetMapping("/quantity")
    public ResponseEntity<List<Loan>> getByQuantity(@RequestParam int quantity) {
        logger.info("Fetching loans by quantity: {}", quantity);
        List<Loan> loans = service.getLoanByQuantity(quantity);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    // POST: Crear nuevo préstamo
    @PostMapping
    public ResponseEntity<Loan> addLoan(@Valid @RequestBody Loan loan) {
        logger.info("Adding loan for customer: {}", loan.getCustomerName());
        Loan newLoan = service.saveLoan(loan);
        return new ResponseEntity<>(newLoan, HttpStatus.CREATED);
    }

    // PUT: Actualizar préstamo por ID
    @PutMapping("/{id}")
    public ResponseEntity<Loan> updateLoan(@PathVariable long id, @Valid @RequestBody Loan loanDetails)
            throws LoanNotFoundException {
        logger.info("Updating loan ID: {}", id);
        Loan updatedLoan = service.updateLoan(id, loanDetails);
        return new ResponseEntity<>(updatedLoan, HttpStatus.OK);
    }

    // PATCH: Actualización parcial
    @PatchMapping("/{id}")
    public ResponseEntity<Loan> updateLoanPartial(@PathVariable long id, @RequestBody Map<String, Object> updates) {
        logger.info("Partially updating loan ID: {}", id);
        Loan updatedLoan = service.updateLoanPartial(id, updates);
        return new ResponseEntity<>(updatedLoan, HttpStatus.OK);
    }

    // DELETE: Eliminar préstamo por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable long id) throws LoanNotFoundException {
        logger.info("Deleting loan ID: {}", id);
        service.deleteLoan(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
