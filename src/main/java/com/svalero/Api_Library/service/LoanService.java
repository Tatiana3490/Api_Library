package com.svalero.Api_Library.service;

import com.svalero.Api_Library.domain.Loan;
import com.svalero.Api_Library.exception.LoanNotFoundException;
import com.svalero.Api_Library.repository.LoanRepository;
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


    //Para obtener todas los péstamos
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }


    //Para obtener préstamos por nombre de cliente
    public List<Loan> getLoanByCustomerName(String customerName) {
        return loanRepository.findByCustomerName(customerName);
    }

    //para obtener préstamos por fecha de prestamo
    public List<Loan> getLoanByLoanDate(LocalDate loanDate) {
        return loanRepository.findByLoanDate(loanDate);
    }

    //para obtener préstamos entre 2 fechas
    public List<Loan> getLoansBetweenDates(LocalDate startDate, LocalDate endDate) {
        return loanRepository.findByLoanDateBetween(startDate, endDate);
    }

    //para obtener préstamos por cantidad
    public  List<Loan> getLoanByQuantity(int quantity) {
        return loanRepository.findByQuantity(quantity);
    }

    //para obtener préstamos por id
    public Loan getLoanById(long id) throws LoanNotFoundException {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: "+ id));
    }

    //Para guardar un nuevo préstamos
    public Loan saveLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    //Para eliminar un préstamos por id
    public void deleteLoan(long id) throws LoanNotFoundException {
        if (!loanRepository.existsById(id)){
            throw new LoanNotFoundException("Loan not found with id: " + id);
        }
        loanRepository.deleteById(id);
    }

    //Para actualizar un préstamo por id
    public Loan updateLoan(long id, Loan loanDetails) throws LoanNotFoundException {
        Loan existingLoan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + id));

        //Para actualizar los campos del prestamo existente con los nuevos valores
        existingLoan.setName(loanDetails.getName());
        existingLoan.setCustomerName(loanDetails.getCustomerName());
        existingLoan.setEmail(loanDetails.getEmail());
        existingLoan.setLoanDate(loanDetails.getLoanDate());
        existingLoan.setQuantity(loanDetails.getQuantity());


        return loanRepository.save(existingLoan);
    }

    public Loan updateLoanPartial(long id, Map<String,Object> updates){
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + id));

        updates.forEach((key, value) ->{
            Field field = ReflectionUtils.findField(Loan.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, loan, value);
            }
        });

        return loanRepository.save(loan);

    }







}
