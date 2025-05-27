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
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) { this.bookService = bookService; }

    //Obtener todos los libros
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        logger.info("BEGIN getAllBooks");
        List<Book> books = bookService.getAllBooks();
        logger.info("END getAllBooks - Total books fetched: " + books.size());
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    //Añadir nuevo libro
    @PostMapping
    public ResponseEntity<Book> addBook(@Valid @RequestBody Book book) {
        logger.info("BEGIN addBook - Adding new book: {}", book.getTitle());
        Book newBook = bookService.saveBook(book);
        logger.info("END addBook - Book added with ID: {}", newBook.getId());
        return new ResponseEntity<>(newBook, HttpStatus.CREATED);
    }

    //Obtener un libro por id
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable long id) throws BookNotFoundException {
        logger.info("BEGIN getBookById - Searching book by id: {}", id);
        try {
            Book book = bookService.getBookById(id);
            logger.info("END getBookById - Book fetched: {}", + book.getId());
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (Exception e){
            logger.error("Error in getBookById - Book not found with id: {}", id,e);
            throw e;
        }
    }

    //Buscar un libro por titulo
    @GetMapping("/title")
    public ResponseEntity<List<Book>> getBookByTitle(@RequestParam String title) {
        logger.info("BEGIN getBookByTitle  - Searching book by title: {}", title);
        List<Book> books = bookService.getBookByTitle(title);
        logger.info("END getBookByTitle - Books fetched: {}", books.size());
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    //Buscar un libro por genero
    @GetMapping("/genre")
    public ResponseEntity<List<Book>> getBookByGenre(@RequestParam String genre) {
        logger.info("BEGIN getBookByGenre  - Searching book by genre: {}", genre);
        List<Book> books = bookService.getBookByGenre(genre);
        logger.info("END getBookByGenre - Books fetched: {}", books.size());
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    //Buscar un libro por paginas
    @GetMapping("/pages")
    public ResponseEntity<List<Book>> getBookByPages(@RequestParam int pages) {
        logger.info("BEGIN getBookByPages  - Searching book by pages: {}", pages);
        List<Book> books = bookService.getBookByPages(pages);
        logger.info("END getBookByPages - Books fetched: {}", books.size());
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    //Buscar un libro por precio
    @GetMapping("/price")
    public ResponseEntity<List<Book>> getBookByPrice(@RequestParam double price) {
        logger.info("BEGIN getBookByPrice  - Searching book by price: {}", price);
        List<Book> books = bookService.getBookByPrice(price);
        logger.info("END getBookByPrice - Books fetched: {}", books.size());
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    //Buscar un libro por disponiblidad
    @GetMapping("/available")
    public ResponseEntity<List<Book>> getBookByAvailability(@RequestParam boolean available) {
        logger.info("BEGIN getBookByAvailability  - Searching book by availability: {}", available);
        List<Book> books = bookService.getBookByAvailability(available);
        logger.info("END getBookByAvailability - Books fetched: {}", books.size());
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    // Actualizar un libro por id
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable long id, @Valid @RequestBody Book bookDetails) throws BookNotFoundException {
        logger.info("BEGIN updateBook - Updating book with ID: {}", id);
        try {
            Book updatedBook = bookService.updateBook(id, bookDetails);
            logger.info("END updateBook - Book updated with ID: {}", updatedBook.getId());
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in updateBook - Book not found with ID: {}", id, e);
            throw e;
        }
    }

    // Actualizar parcialmente un libro por id
    @PatchMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable long id, @Valid @RequestBody Map<String, Object> updates) {
        logger.info("BEGIN updateBookPartial - Partially updating book with ID: {}", id);
        try {
            Book updatedBook = bookService.updateBookPartial(id, updates);
            logger.info("END updateBookPartial - Book updated with ID: {}", updatedBook.getId());
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            logger.error("Error in updateBookPartial - Book not found with ID: {}", id, e);
            throw e;
        }
    }

    // Eliminar un libro por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable long id) throws BookNotFoundException {
        logger.info("BEGIN deleteBook - Deleting book with ID: {}", id);
        try {
            bookService.deleteBook(id);
            logger.info("END deleteBook - Book deleted with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error in deleteBook - Book not found with ID: {}", id, e);
            throw e;
        }
    }
}
