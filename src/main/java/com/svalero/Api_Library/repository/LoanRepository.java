package com.svalero.Api_Library.repository;

import com.svalero.Api_Library.domain.Loan;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface LoanRepository extends CrudRepository<Loan, Long> {

    // ================= BÚSQUEDAS BÁSICAS ================= //
    //Metodos para buscar
    List<Loan> findAll();
    List<Loan> findByCustomerName(String customerName);
    List<Loan> findByLoanDate(LocalDate loanDate);
    List<Loan> findByQuantity(int quantity);
    List<Loan> findByLoanDateBetween(LocalDate startDate, LocalDate endDate);


    // ================= CONSULTAS SQL NATIVAS ================= //
    @Query(value = "SELECT * FROM loans WHERE quantity >= :quantity", nativeQuery = true)
    List<Loan> findLoansWithQuantityGreaterThan(@Param("quantity") int quantity);

}
