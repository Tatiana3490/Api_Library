package com.svalero.Api_Library.repository;

import com.svalero.Api_Library.domain.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends CrudRepository<Book, Long> {

    // ================= BÚSQUEDAS BÁSICAS ================= //

    List<Book> findAll();
    List<Book> findByTitle(String title);
    List<Book> findByGenre(String genre);
    List<Book> findByPages(int page);
    List<Book> findByPrice(double price);
    List<Book> findByAvailable(boolean available);

    // ================= CONSULTAS PERSONALIZADAS (JPQL) ================= //

    @Query("SELECT b FROM Book b WHERE b.pages > :pages")
    List<Book> findBooksWithPagesGreaterThan(@Param("pages") int pages);

    @Query("SELECT b FROM Book b WHERE b.price < :price")
    List<Book> findBooksWithPriceLessThan(@Param("price") float price);

    @Query("SELECT b FROM Book b WHERE LOWER(b.genre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> findBooksWithGenreLike(@Param("keyword") String keyword);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')) AND b.available = :available AND LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))")
    List<Book> findBooksByTitleAndAvailableAndGenre(
            @Param("title") String title,
            @Param("available") boolean available,
            @Param("genre") String genre
    );

    // ================= CONSULTAS SQL NATIVAS ================= //

    @Query(value = "SELECT * FROM book WHERE price > :price", nativeQuery = true)
    List<Book> findBooksWithPriceGreaterThanNative(@Param("price") float price);


}
