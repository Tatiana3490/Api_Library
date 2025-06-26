package com.svalero.Api_Library.controller;

import com.svalero.Api_Library.DTO.LoanDTO;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final Logger logger = LoggerFactory.getLogger(LoanController.class);
    private final LoanService loanService;

    @Autowired
    public LoanController(LoanService service) {
        this.loanService = service;
    }



    // GET: Listar todos los préstamos
    @GetMapping
    public List<LoanDTO> getAllLoans() {
        logger.info("Fetching all loans");
        return loanService.getAllLoans().stream()
                .map(loanService::convertToDTO)
                .collect(Collectors.toList());
    }

    // GET: Buscar préstamo por ID
    @GetMapping("/{id}")
    public ResponseEntity<LoanDTO> getLoanById(@PathVariable long id) throws LoanNotFoundException {
        logger.info("Fetching loan by ID: {}", id);
        Loan loan = loanService.getLoanById(id);
        LoanDTO loanDTO = loanService.convertToDTO(loan);
        return new ResponseEntity<>(loanDTO, HttpStatus.OK);
    }

    // GET: Buscar préstamos por nombre de cliente
    @GetMapping("/customer-name")
    public ResponseEntity<List<LoanDTO>> getByCustomerName(@RequestParam String customerName) {
        logger.info("Fetching loans for customer: {}", customerName);
        List<LoanDTO> loanDTOs = loanService.getLoanByCustomerName(customerName).stream()
                .map(loanService::convertToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(loanDTOs, HttpStatus.OK);
    }

    // GET: Buscar préstamos por fecha
    @GetMapping("/loan-date")
    public ResponseEntity<List<LoanDTO>> getByLoanDate(@RequestParam LocalDate loanDate) {
        logger.info("Fetching loans for loan date: {}", loanDate);
        List<LoanDTO> loanDTOs = loanService.getLoanByLoanDate(loanDate).stream()
                .map(loanService::convertToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(loanDTOs, HttpStatus.OK);
    }

    // GET: Buscar préstamos entre dos fechas
    @GetMapping("/range")
    public ResponseEntity<List<LoanDTO>> getLoansBetweenDates(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {
        logger.info("Fetching loans from {} to {}", startDate, endDate);
        List<LoanDTO> loanDTOs = loanService.getLoansBetweenDates(startDate, endDate).stream()
                .map(loanService::convertToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(loanDTOs, HttpStatus.OK);
    }

    // GET: Buscar préstamos por cantidad
    @GetMapping("/quantity")
    public ResponseEntity<List<LoanDTO>> getByQuantity(@RequestParam int quantity) {
        logger.info("Fetching loans by quantity: {}", quantity);
        List<LoanDTO> loanDTOs = loanService.getLoanByQuantity(quantity).stream()
                .map(loanService::convertToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(loanDTOs, HttpStatus.OK);
    }

    // POST: Crear nuevo préstamo
    @PostMapping
    public ResponseEntity<LoanDTO> addLoan(@Valid @RequestBody Loan loan) {
        logger.info("Adding loan for customer: {}", loan.getCustomerName());
        Loan newLoan = loanService.saveLoan(loan);
        LoanDTO dto = loanService.convertToDTO(newLoan);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // PUT: Actualizar préstamo completo
    @PutMapping("/{id}")
    public ResponseEntity<LoanDTO> updateLoan(
            @PathVariable long id,
            @Valid @RequestBody Loan loanDetails) throws LoanNotFoundException {
        logger.info("Updating loan ID: {}", id);
        Loan updatedLoan = loanService.updateLoan(id, loanDetails);
        LoanDTO dto = loanService.convertToDTO(updatedLoan);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    // PATCH: Actualización parcial
    @PatchMapping("/{id}")
    public ResponseEntity<LoanDTO> updateLoanPartial(
            @PathVariable long id,
            @RequestBody Map<String, Object> updates) {
        logger.info("Partially updating loan ID: {}", id);
        Loan updatedLoan = loanService.updateLoanPartial(id, updates);
        LoanDTO dto = loanService.convertToDTO(updatedLoan);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    // DELETE: Eliminar préstamo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable long id) throws LoanNotFoundException {
        logger.info("Deleting loan ID: {}", id);
        loanService.deleteLoan(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ========== CONSULTA SQL native ==========
    @GetMapping("/quantity/greater")
    public ResponseEntity<List<Loan>> getLoansWithQuantityGreaterThan(@RequestParam int quantity) {
        List<Loan> loans = loanService.findLoansWithQuantityGreaterThan(quantity);
        return ResponseEntity.ok(loans);
    }
}

