package com.svalero.Api_Library.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanDTO {
    private long id;
    private String name;
    private String customerName;
    private String email;
    private LocalDate loanDate;
    private int quantity;
    private BookDTO book;

}
