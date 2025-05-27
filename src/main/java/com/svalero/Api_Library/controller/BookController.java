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

    // GET: Listar todos los libros
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        logger.info("Fetching all books");
        List<Book> books = service.getAllBooks();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    // GET: Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable long id) throws BookNotFoundException {
        logger.info("Searching book by ID: {}", id);
        Book book = service.getBookById(id);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    // GET: Buscar por título
    @GetMapping("/title")
    public ResponseEntity<List<Book>> getBookByTitle(@RequestParam String title) {
        logger.info("Searching books by title: {}", title);
        List<Book> books = service.getBookByTitle(title);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    // GET: Buscar por género
    @GetMapping("/genre")
    public ResponseEntity<List<Book>> getBookByGenre(@RequestParam String genre) {
        logger.info("Searching books by genre: {}", genre);
        List<Book> books = service.getBookByGenre(genre);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    // GET: Buscar por número de páginas
    @GetMapping("/pages")
    public ResponseEntity<List<Book>> getBookByPages(@RequestParam int pages) {
        logger.info("Searching books by pages: {}", pages);
        List<Book> books = service.getBookByPages(pages);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    // GET: Buscar por precio
    @GetMapping("/price")
    public ResponseEntity<List<Book>> getBookByPrice(@RequestParam double price) {
        logger.info("Searching books by price: {}", price);
        List<Book> books = service.getBookByPrice(price);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    // GET: Buscar por disponibilidad
    @GetMapping("/available")
    public ResponseEntity<List<Book>> getBookByAvailability(@RequestParam boolean available) {
        logger.info("Searching books by availability: {}", available);
        List<Book> books = service.getBookByAvailability(available);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    // POST: Crear un nuevo libro
    @PostMapping
    public ResponseEntity<Book> addBook(@Valid @RequestBody Book book) {
        logger.info("Adding new book: {}", book.getTitle());
        Book newBook = service.saveBook(book);
        return new ResponseEntity<>(newBook, HttpStatus.CREATED);
    }

    // PUT: Actualizar libro por ID
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable long id, @Valid @RequestBody Book bookDetails)
            throws BookNotFoundException {
        logger.info("Updating book ID: {}", id);
        Book updatedBook = service.updateBook(id, bookDetails);
        return new ResponseEntity<>(updatedBook, HttpStatus.OK);
    }

    // PATCH: Actualizar parcialmente por ID
    @PatchMapping("/{id}")
    public ResponseEntity<Book> updateBookPartial(@PathVariable long id, @RequestBody Map<String, Object> updates) {
        logger.info("Partially updating book ID: {}", id);
        Book updatedBook = service.updateBookPartial(id, updates);
        return ResponseEntity.ok(updatedBook);
    }

    // DELETE: Eliminar libro por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable long id) throws BookNotFoundException {
        logger.info("Deleting book ID: {}", id);
        service.deleteBook(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
