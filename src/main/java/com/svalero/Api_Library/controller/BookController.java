package com.svalero.Api_Library.controller;

import com.svalero.Api_Library.domain.Book;
import com.svalero.Api_Library.exception.BookNotFoundException;
import com.svalero.Api_Library.service.BookService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {

    private final Logger logger = LoggerFactory.getLogger(BookController.class);
    private final BookService service;

    @Autowired
    public BookController(BookService service) {
        this.service = service;
    }

    // ========== GET: Consultas básicas ==========

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        logger.info("Fetching all books");
        return new ResponseEntity<>(service.getAllBooks(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable long id) throws BookNotFoundException {
        logger.info("Fetching book by ID: {}", id);
        return new ResponseEntity<>(service.getBookById(id), HttpStatus.OK);
    }

    @GetMapping("/title")
    public ResponseEntity<List<Book>> getBooksByTitle(@RequestParam String title) {
        logger.info("Fetching books by title: {}", title);
        return new ResponseEntity<>(service.getBookByTitle(title), HttpStatus.OK);
    }

    @GetMapping("/genre")
    public ResponseEntity<List<Book>> getBooksByGenre(@RequestParam String genre) {
        logger.info("Fetching books by genre: {}", genre);
        return new ResponseEntity<>(service.getBookByGenre(genre), HttpStatus.OK);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Book>> getBooksByAvailable(@RequestParam boolean available) {
        logger.info("Fetching books by availability: {}", available);
        return new ResponseEntity<>(service.getBookByAvailability(available), HttpStatus.OK);
    }

    // ========== GET: Filtros combinados (3 campos) ==========

    @GetMapping("/search")
    public ResponseEntity<List<Book>> getBooksByTitleAndAvailableAndGenre(
            @RequestParam String title,
            @RequestParam boolean available,
            @RequestParam String genre) {
        logger.info("Fetching books by title={}, available={}, genre={}", title, available, genre);
        return new ResponseEntity<>(
                service.findBooksByTitleAndAvailableAndGenre(title, available, genre),
                HttpStatus.OK
        );
    }

    // ========== GET: JPQL ==========

    @GetMapping("/pages-greater-than")
    public ResponseEntity<List<Book>> getBooksWithPagesGreaterThan(@RequestParam int pages) {
        logger.info("Fetching books with more than {} pages", pages);
        return new ResponseEntity<>(service.findBooksWithPagesGreaterThan(pages), HttpStatus.OK);
    }

    @GetMapping("/price-less-than")
    public ResponseEntity<List<Book>> getBooksWithPriceLessThan(@RequestParam float price) {
        logger.info("Fetching books with price less than {}", price);
        return new ResponseEntity<>(service.findBooksWithPriceLessThan(price), HttpStatus.OK);
    }

    @GetMapping("/genre-contains")
    public ResponseEntity<List<Book>> getBooksWithGenreLike(@RequestParam String keyword) {
        logger.info("Fetching books where genre contains '{}'", keyword);
        return new ResponseEntity<>(service.findBooksWithGenreLike(keyword), HttpStatus.OK);
    }

    // ========== POST: Crear libro ==========

    @PostMapping
    public ResponseEntity<Book> addBook(@Valid @RequestBody Book book) {
        logger.info("Adding new book: {}", book.getTitle());
        return new ResponseEntity<>(service.saveBook(book), HttpStatus.CREATED);
    }

    // ========== PUT: Actualización completa ==========

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable long id, @Valid @RequestBody Book book)
            throws BookNotFoundException {
        logger.info("Updating book with ID: {}", id);
        return new ResponseEntity<>(service.updateBook(id, book), HttpStatus.OK);
    }

    // ========== PATCH: Actualización parcial ==========

    @PatchMapping("/{id}")
    public ResponseEntity<Book> updateBookPartial(@PathVariable long id, @RequestBody Map<String, Object> updates)
            throws BookNotFoundException {
        logger.info("Partially updating book with ID: {}", id);
        return new ResponseEntity<>(service.updateBookPartial(id, updates), HttpStatus.OK);
    }

    // ========== DELETE: Eliminar libro ==========

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable long id) throws BookNotFoundException {
        logger.info("Deleting book with ID: {}", id);
        service.deleteBook(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/price-greater-than-native")
    public ResponseEntity<List<Book>> getBooksWithPriceGreaterThanNative(@RequestParam float price) {
        logger.info("Fetching books with native SQL price > {}", price);
        return new ResponseEntity<>(service.findBooksWithPriceGreaterThanNative(price), HttpStatus.OK);
    }

}
