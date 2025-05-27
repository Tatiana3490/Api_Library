package com.svalero.Api_Library.service;

import com.svalero.Api_Library.domain.Author;
import com.svalero.Api_Library.domain.Book;
import com.svalero.Api_Library.domain.BookCategory;
import com.svalero.Api_Library.exception.BookCategoryNotFoundException;
import com.svalero.Api_Library.repository.BookCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class BookCategoryService {
    private final BookCategoryRepository bookCategoryRepository;

    @Autowired
    public BookCategoryService(BookCategoryRepository bookCategoryRepository) {
        this.bookCategoryRepository = bookCategoryRepository;
    }

    //para obtener todas las categorias
    public List<BookCategory> getAllBookCategories() {
        return bookCategoryRepository.findAll();
    }

    //para obtener las categorias de libros por id
    public BookCategory getBookCategoriesById(Long id) throws BookCategoryNotFoundException {
        return bookCategoryRepository.findById(id)
                .orElseThrow(() ->new RuntimeException("Book Category not found with id: "+ id));

    }

    //Para obtener las categorias por nombre
    public List<BookCategory> getBookCategoriesByName(String name) throws BookCategoryNotFoundException {
        return bookCategoryRepository.findByName(name);
    }

    //Para obtener las categorias de libros por descripción
    public List<BookCategory> getBookCategoriesByDescription(String description) throws BookCategoryNotFoundException {
        return bookCategoryRepository.findByDescription(description);
    }

    //para obtener las categorias de libros activas
    public List<BookCategory> getBookCategoriesByActive(Boolean active) throws BookCategoryNotFoundException {
        return bookCategoryRepository.findByActive(active);
    }

    //Para obtener la fecha de creación de la categoria del libro
    public List<BookCategory> getBookCategoriesByCreateDate(LocalDate createDate) throws BookCategoryNotFoundException {
        return bookCategoryRepository.findByCreatedDate(createDate);
    }

    //para obtener el numero de libros
    public List<BookCategory> getBookCategoriesByNumberBooks(int numberBooks) throws BookCategoryNotFoundException {
        return bookCategoryRepository.findByNumberBooks(numberBooks);
    }

    //para buscar categorias por número mínimo de libros
    public List<BookCategory> getBookCategoriesWithMinBooks(int minBooks) throws BookCategoryNotFoundException {
        return bookCategoryRepository.findByNumberBooksGreaterThan(minBooks);
    }

    //para guardar una categoria de libro
    public BookCategory saveBookCategory(BookCategory bookCategory) throws BookCategoryNotFoundException {
        return bookCategoryRepository.save(bookCategory);
    }

    //Para eliminar una BookCategory por id
    public void deleteBookCategory(long id) throws BookCategoryNotFoundException {
        if (!bookCategoryRepository.existsById(id)){
            throw new BookCategoryNotFoundException("Book Category not found with id: "+ id);
        }
        bookCategoryRepository.deleteById(id);
    }

    //Para actualizar la categoria por id
    public BookCategory updateBookCategory(long id, BookCategory bookCategoryDetails) throws BookCategoryNotFoundException {
        BookCategory existingBookCategory = bookCategoryRepository.findById(id)
                .orElseThrow(() ->new RuntimeException("Book Category not found with id: "+ id));

        //Para actualizar los campos de las categorias existentes con los nuevos valores
        existingBookCategory.setName(bookCategoryDetails.getName());
        existingBookCategory.setDescription(bookCategoryDetails.getDescription());
        existingBookCategory.setActive(bookCategoryDetails.getActive());
        existingBookCategory.setCreatedDate(bookCategoryDetails.getCreatedDate());
        existingBookCategory.setNumberBooks(bookCategoryDetails.getNumberBooks());

        return bookCategoryRepository.save(existingBookCategory);
    }

    public BookCategory updateBookCategoryPartial(long id, Map<String, Object> updates) {
        BookCategory bookCategory = bookCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book Category not found with id: " + id));

        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Author.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, bookCategory, value);
            }
        });

        return bookCategoryRepository.save(bookCategory);
    }



}
