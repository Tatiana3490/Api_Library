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
    private final LoanService loanService;

    @Autowired
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    //Para obtener todos los préstamos
    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        logger.info("BEGIN getAllLoans");
        List<Loan> loans = loanService.getAllLoans();
        logger.info("END getAllLoans - Total reservations fetched: " + loans.size());
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    //para buscar préstamos por nombre del usuario
    @GetMapping("/customer-name")
    public ResponseEntity<List<Loan>> getLoanByCustomerName(@RequestParam String customerName) {
        logger.info("BEGIN getLoanByCustomerName - Searching loans for customer: " + customerName);
        List<Loan> loans = loanService.getLoanByCustomerName(customerName);
        logger.info("END getLoanByCustomerName - Total loans found: ", loans.size());
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    //Para buscar préstamos por fecha del préstamo
    @GetMapping("/loan-date")
    public ResponseEntity<List<Loan>> getLoanByLoanDate(@RequestParam LocalDate loanDate) {
        logger.info("BEGIN getLoanByLoanDate - Searching loans for date: {} " + loanDate);
        List<Loan> loans = loanService.getLoanByLoanDate(loanDate);
        logger.info("END getLoanByLoanDate - Total loans found: {}", loans.size());
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    //Para buscar préstamos entre 2 fechas
    @GetMapping("/range")
    public ResponseEntity<List<Loan>> getLoansBetweenDates(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        logger.info("BEGIN getLoansBetweenDates - Searching loans between {} and {}", startDate, endDate);
        List<Loan> loans = loanService.getLoansBetweenDates(startDate, endDate);
        logger.info("END getLoansBetweenDates - Total loans found: {}", loans.size());
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    //para obtener préstamos por cantidad de entradas
    @GetMapping("/quantity")
    public ResponseEntity<List<Loan>> getLoanByQuantity(@RequestParam int quantity) {
        logger.info("BEGIN getLoanByQuantity - Searching loans for quantity: {}", quantity);
        List<Loan> loans = loanService.getLoanByQuantity(quantity);
        logger.info("END getLoanByQuantity - Total loans found: {}", loans.size());
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    //para obtener préstamos por id
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable int id) throws LoanNotFoundException {
        logger.info("BEGIN getLoanById - Searching loans for id: {}", id);
        try {
            Loan loan = loanService.getLoanById(id);
            logger.info("END getLoanById - Loan found: {}", loan.getId());
            return new ResponseEntity<>(loan, HttpStatus.OK);
        }catch (Exception e) {
            logger.info("END getLoanById - Loan not found: {}", id);
            throw e;
        }
    }

    //Añadir nuevo préstamo
    @PostMapping
    public ResponseEntity<Loan> addLoan(@Valid @RequestBody Loan loan) {
        logger.info("BEGIN addLoan - Adding loans for user: {}", loan.getCustomerName());
        Loan newLoan = loanService.saveLoan(loan);
        logger.info("END addLoan - Adding loans with id: {}", newLoan.getId());
        return new ResponseEntity<>(newLoan, HttpStatus.CREATED);
    }

    //Eliminar un préstamo por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable int id) throws LoanNotFoundException {
        logger.info("BEGIN deleteLoan - Deleting loan with id: {}", id);
        try {
            loanService.deleteLoan(id);
            logger.info("END deleteLoan - Loan deleted with id: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (Exception e) {
            logger.info("Error in deleteLoan - Loan not found: {}", id, e);
            throw e;
        }
    }

    //para actualizar un préstamo por id
    @PutMapping("/{id}")
    public ResponseEntity<Loan> updateLoan(@PathVariable int id, @Valid @RequestBody Loan loanDetails) throws LoanNotFoundException {
        logger.info("BEGIN updateLoan - Updating loan with id: {}", id);
        try {
            Loan updatedLoan = loanService.updateLoan(id, loanDetails);
            logger.info("END updateLoan - Updating loan with id: {}", updatedLoan.getId());
            return new ResponseEntity<>(updatedLoan, HttpStatus.OK);
        } catch (Exception e) {
            logger.info("Error in updateLoan - Loan not found with id: {}", id, e);
            throw e;
        }
    }

    //para actualizar parcialmente un préstamo por id
    @PatchMapping("/{id}")
    public ResponseEntity<Loan> updateLoan(@PathVariable long id, @Valid @RequestBody Map<String, Object> updates) {
        logger.info("BEGIN updateLoanPartial - Partially updating loan with ID: {}", id);
        try {
            Loan updatedLoan = loanService.updateLoanPartial(id, updates);
            logger.info("END updateLoanPartial - Loan updated with id: {}", updatedLoan.getId());
            return new ResponseEntity<>(updatedLoan, HttpStatus.OK);
        } catch (Exception e) {
            logger.info("Error in updateLoanPartial - Loan not found with id: {}", id, e);
            throw e;
        }
    }

    //para manejar excepciones de recurso no encontrado
    @ExceptionHandler(LoanNotFoundException.class)
    public ResponseEntity<Void> handleLoanNotFoundException(LoanNotFoundException exception) {
        logger.info("Handling handleLoanNotFoundException - {}", exception.getMessage(), exception);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
