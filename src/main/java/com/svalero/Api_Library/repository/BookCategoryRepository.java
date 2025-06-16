package com.svalero.Api_Library.repository;

import com.svalero.Api_Library.domain.BookCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookCategoryRepository extends CrudRepository<BookCategory, Long> {
    // ================= BÚSQUEDAS BÁSICAS ================= //
    //Metodos para buscar
    List<BookCategory> findAll();
    List<BookCategory> findByName(String name);
    List<BookCategory> findByDescription(String description);
    List<BookCategory> findByActive(boolean active);
    List<BookCategory> findByCreatedDate(LocalDate createdDate);
    List<BookCategory> findByNumberBooks(int numberBooks);
    List<BookCategory> findByNumberBooksGreaterThan(int numberBooks);
}
