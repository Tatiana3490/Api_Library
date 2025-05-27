package com.svalero.Api_Library.repository;

import com.svalero.Api_Library.domain.Book;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends CrudRepository<Book, Long> {
    //Metodos para buscar
    List<Book> findAll();
    List<Book> findByTitle(String title);
    List<Book> findByGenre(String genre);
    List<Book> findByPages(int page);
    List<Book> findByPrice(double price);
    List<Book> findByAvailable(boolean available);


}
