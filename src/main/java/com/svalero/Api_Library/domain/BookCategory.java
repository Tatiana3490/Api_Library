package com.svalero.Api_Library.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "BookCategory")
@Table(name = "book_categories")

public class BookCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull(message = "Name is required")
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private Boolean active = true;
    @NotNull(message = "Date is required")
    @Column(name = "creation_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    @Column(name = "number_books", nullable = false)
    private int numberBooks;

    @OneToMany(mappedBy = "category")
    @JsonBackReference(value = "book-category")
    private List<Book> books;

}