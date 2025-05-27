package com.svalero.Api_Library.controller;

import com.svalero.Api_Library.domain.BookCategory;
import com.svalero.Api_Library.exception.BookCategoryNotFoundException;
import com.svalero.Api_Library.service.BookCategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("book-categories")
public class BookCategoryController {

    private final Logger logger = LoggerFactory.getLogger(BookCategoryController.class);
    private final BookCategoryService bookCategoryService;

    @Autowired
    public BookCategoryController(BookCategoryService bookCategoryService) {
        this.bookCategoryService = bookCategoryService;
    }

    //Obtener todas las categorías de libro
    @GetMapping
    public ResponseEntity<List<BookCategory>> getAllBookCategories() {
        logger.info("BEGIN getAllBookCategories");
        List<BookCategory> bookCategories = bookCategoryService.getAllBookCategories();
        logger.info("END getAllBookCategories - total book categories: {}" + bookCategories.size());
        return new ResponseEntity<>(bookCategories, HttpStatus.OK);
    }

    //Obtener categorías por id
    @GetMapping("/{id}")
    public ResponseEntity<BookCategory> getBookCategoryById(@PathVariable long id) throws BookCategoryNotFoundException {
        logger.info("BEGIN getBookCategoryById - Searching book category by id: {}", id);
        try {
            BookCategory bookCategory = bookCategoryService.getBookCategoriesById(id);
            logger.info("END getBookCategoryById - Book category fetched: {}", + bookCategory.getId());
            return new ResponseEntity<>(bookCategory, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getBookCategoryById - BookCategory not found with id: {}",id, e);
            throw e;
        }
    }

    //Obtener categorias por nombre
    @GetMapping("/name")
    public ResponseEntity<List<BookCategory>> getBookCategoryByName(@RequestParam String name) {
        logger.info("BEGIN getBookCategoryByName - Searching book category by name: {}", name);
        List<BookCategory> bookCategories = bookCategoryService.getBookCategoriesByName(name);
        logger.info("END getBookCategoryByName - Book category fetched: {}", + bookCategories.size());
        return new ResponseEntity<>(bookCategories, HttpStatus.OK);
    }

    //Obtener categorias de libros por descripción
    @GetMapping("/description")
    public ResponseEntity<List<BookCategory>> getBookCategoryByDescription(@RequestParam String description) {
        logger.info("BEGIN getBookCategoryByDescription - Searching book category by description: {}", description);
        List<BookCategory> bookCategories = bookCategoryService.getBookCategoriesByDescription(description);
        logger.info("END getBookCategoryByDescription - Book category fetched: {}", + bookCategories.size());
        return new ResponseEntity<>(bookCategories, HttpStatus.OK);
    }

    //Obtener categorias activas/inactivas
    @GetMapping("/active")
    public ResponseEntity<List<BookCategory>> getBookCategoryByActive(
            @RequestParam(defaultValue = "true") boolean active) {
        logger.info("BEGIN getBookCategoryByActive - Fetching {} book categories", active ? "active" : "inactive");
        List<BookCategory> bookCategories = bookCategoryService.getBookCategoriesByActive(active);
        logger.info("END getBookCategoryByActive - Book category fetched: {}", bookCategories.size());
        return new ResponseEntity<>(bookCategories, HttpStatus.OK);
    }


    //Obtener categrias por fecha de creación
    @GetMapping("/creation-date")
    public ResponseEntity<List<BookCategory>> getBookCategoriesByCreateDate(@RequestParam LocalDate date) {
        logger.info("BEGIN getBookCategoriesByCreateDate - Searching categories created on: {}", date);
        List<BookCategory> bookCategories = bookCategoryService.getBookCategoriesByCreateDate(date);
        logger.info("END getBookCategoriesByCreateDate - Total categories found: {}", bookCategories.size());
        return new ResponseEntity<>(bookCategories, HttpStatus.OK);
    }

    //Obtener categorías con un número minimo de libros
    @GetMapping("/min-books")
    public ResponseEntity<List<BookCategory>> getBookCategoriesWithMinBooks(@RequestParam int minBooks) {
        logger.info("BEGIN getBookCategoriesByMinBooks - Searching categories min of: {}", minBooks);
        List<BookCategory> bookCategories = bookCategoryService.getBookCategoriesWithMinBooks(minBooks);
        logger.info("END getBookCategoriesByMinBooks - Total categories found: {}", bookCategories.size());
        return new ResponseEntity<>(bookCategories, HttpStatus.OK);
    }

    //para agregar una nueva categoría
    @PostMapping
    public ResponseEntity<BookCategory> addBookCategory(@Valid @RequestBody BookCategory bookCategory) {
        logger.info("BEGIN addBookCategory - Adding book category: {}", bookCategory.getName());
        BookCategory newCategory = bookCategoryService.saveBookCategory(bookCategory);
        logger.info("END addBookCategory - Book category added with id: {}", + newCategory.getId());
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    //para obtener el numero de libros que hay en una categoría
    @GetMapping("/number-books")
    public ResponseEntity<List<BookCategory>> getBookCategoriesByNumberBooks(@RequestParam int numberBooks) {
        logger.info("BEGIN getBookCategoriesByNumberBooks - Searching categories min of: {}", numberBooks);
        List<BookCategory> bookCategories = bookCategoryService.getBookCategoriesByNumberBooks(numberBooks);
        logger.info("END getBookCategoriesByNumberBooks - Total books found: {}", bookCategories.size());
        return new ResponseEntity<>(bookCategories, HttpStatus.OK);
    }

    //para actualizar la categoría por id
    @PutMapping("/{id}")
    public ResponseEntity<BookCategory> updateBookCategory(@PathVariable long id, @Valid @RequestBody BookCategory bookCategory)
            throws BookCategoryNotFoundException {
        logger.info("BEGIN updateBookCategory - Searching book category by id: {}", id);
        try {
            BookCategory updatedBookCategory = bookCategoryService.updateBookCategory(id, bookCategory);
            logger.info("END updateBookCategory - Book category fetched: {}", + updatedBookCategory.getId());
            return new ResponseEntity<>(updatedBookCategory, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in updateBookCategory - Book category not found with id: {}",id, e);
            throw e;
        }

    }

    //para actualizar parcialmente una categoría por id
    @PatchMapping("/{id}")
    public ResponseEntity<BookCategory> updateBookCategoryPartial(@PathVariable Long id, @Valid @RequestBody Map<String, Object> updates) throws BookCategoryNotFoundException {
        logger.info("BEGIN updateBookCategory - Partially updating book category by id: {}", id);
        try {
            BookCategory updatedBookCategory = bookCategoryService.updateBookCategoryPartial(id, updates);
            logger.info("END updateBookCategory - Book category fetched: {}", +updatedBookCategory.getId());
            return new ResponseEntity<>(updatedBookCategory, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in updateBookCategory - Book category not found with id: {}", id, e);
            throw e;
        }
    }
        //para eliminar una categoría por id
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteBookCategory ( @PathVariable long id) throws BookCategoryNotFoundException {
            logger.info("BEGIN deleteBookCategory - Deleting book category by id: {}", id);
            try {
                bookCategoryService.deleteBookCategory(id);
                logger.info("END deleteBookCategory - Book category deleted whit id: {}", id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (Exception e) {
                logger.error("Error in deleteBookCategory - Book category not found with id: {}", id, e);
                throw e;
            }
        }


    //para manejar excepciones de categoría de libro no encontrada
    @ExceptionHandler(BookCategoryNotFoundException.class)
    public ResponseEntity<String> handleBookCategoryNotFoundException(BookCategoryNotFoundException exception) {
       logger.error("Handling BookCategoryNotFoundException: {}", exception.getMessage(), exception);
       return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }
}
