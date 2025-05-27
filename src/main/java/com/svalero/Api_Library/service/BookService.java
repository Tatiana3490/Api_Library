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
    private BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    //Para obtener todos los libros
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    //Para obtener libros por id
    public Book getBookById(long id) throws BookNotFoundException {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    //Para obtener libros por titulo
    public List<Book> getBookByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    //Para obtener libros por género
    public List<Book> getBookByGenre(String genre) {
        return bookRepository.findByGenre(genre);
    }

    //Para obtener los libros por numero de paginas
    public List<Book> getBookByPages(int pages) {
        return bookRepository.findByPages(pages);
    }

    //Para obtener los libros por precio
    public List<Book> getBookByPrice(double price) {
        return bookRepository.findByPrice(price);
    }

    //Para obtener los libros disponibles
    public List<Book> getBookByAvailability(boolean availability) {
        return bookRepository.findByAvailable(availability);
    }

    //Para guardar un libro
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    //Para actualizar un libro
    public Book updateBook(Long id, Book bookDetails) throws BookNotFoundException {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: "+ id));

        //para actualizar los campos de Book existentes con los nuevos valores
        existingBook.setTitle(bookDetails.getTitle());
        existingBook.setGenre(bookDetails.getGenre());
        existingBook.setPages(bookDetails.getPages());
        existingBook.setPrice(bookDetails.getPrice());
        existingBook.setAvailable(bookDetails.isAvailable());

        return bookRepository.save(existingBook);
    }

    public Book updateBookPartial(Long id, Map<String, Object> updates){
        Book book =bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " +id));

        updates.forEach((key, value) ->{
            Field field = ReflectionUtils.findField(Book.class, key);
            if(field != null){
                field.setAccessible(true);
                ReflectionUtils.setField(field, book, value);
            }
        });

        return bookRepository.save(book);
    }

    //Para eliminar un libro por id
    public void deleteBook(Long id) throws BookNotFoundException {
        if (!bookRepository.existsById(id)){
            throw new BookNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }



}
