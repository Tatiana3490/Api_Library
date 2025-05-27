package com.svalero.Api_Library.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Loan")
@Table(name = "loans")

public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, unique = true)
    private String name;
    @NotNull(message = "Customer name is required")
    @Column(name="customer_name", nullable = false, unique = true)
    private String customerName;
    @Column
    private String email;
    @Column(name = "loan_date",nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate loanDate;
    @NotNull(message = "Quantity is required")
    @Column(nullable = false)
    private int quantity ;


    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

}
