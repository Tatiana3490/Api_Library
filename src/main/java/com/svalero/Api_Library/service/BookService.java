package com.svalero.Api_Library.service;

import com.svalero.Api_Library.domain.Book;
import com.svalero.Api_Library.exception.BookNotFoundException;
import com.svalero.Api_Library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Service
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // ===================== LECTURA (READ) =====================

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(long id) throws BookNotFoundException {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    public List<Book> getBookByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    public List<Book> getBookByGenre(String genre) {
        return bookRepository.findByGenre(genre);
    }

    public List<Book> getBookByPages(int pages) {
        return bookRepository.findByPages(pages);
    }

    public List<Book> getBookByPrice(double price) {
        return bookRepository.findByPrice(price);
    }

    public List<Book> getBookByAvailability(boolean availability) {
        return bookRepository.findByAvailable(availability);
    }

    // ===================== CREACIÓN (CREATE) =====================

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    // ===================== ACTUALIZACIÓN COMPLETA (UPDATE) =====================

    public Book updateBook(Long id, Book bookDetails) throws BookNotFoundException {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        existingBook.setTitle(bookDetails.getTitle());
        existingBook.setGenre(bookDetails.getGenre());
        existingBook.setPages(bookDetails.getPages());
        existingBook.setPrice(bookDetails.getPrice());
        existingBook.setAvailable(bookDetails.isAvailable());

        return bookRepository.save(existingBook);
    }

    // ===================== ACTUALIZACIÓN PARCIAL (PATCH) =====================

    public Book updateBookPartial(Long id, Map<String, Object> updates) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Book.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, book, value);
            }
        });

        return bookRepository.save(book);
    }

    // ===================== ELIMINACIÓN (DELETE) =====================

    public void deleteBook(Long id) throws BookNotFoundException {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    // ===================== CONSULTAS JPQL =====================

    public List<Book> findBooksWithPagesGreaterThan(int pages) {
        return bookRepository.findBooksWithPagesGreaterThan(pages);
    }

    public List<Book> findBooksWithPriceLessThan(float price) {
        return bookRepository.findBooksWithPriceLessThan(price);
    }

    public List<Book> findBooksWithGenreLike(String keyword) {
        return bookRepository.findBooksWithGenreLike(keyword);
    }

    public List<Book> findBooksByTitleAndAvailableAndGenre(String title, boolean available, String genre) {
        return bookRepository.findBooksByTitleAndAvailableAndGenre(title, available, genre);
    }


}
