package com.svalero.Api_Library.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name ="Book")
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull(message = "Book name is required")
    @Column(name = "title", nullable = false, unique = true)
    private String title;
    @Column
    private String genre;
    @Column
    private int pages;
    @Column
    private double price;
    @Column
    private boolean available;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonManagedReference(value = "category_book")
    private BookCategory category;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonManagedReference(value = "author_book")
    private Author author;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Loan> loans;

}
