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
@RequestMapping("/book-categories")
public class BookCategoryController {

    private final Logger logger = LoggerFactory.getLogger(BookCategoryController.class);
    private final BookCategoryService service;

    @Autowired
    public BookCategoryController(BookCategoryService service) {
        this.service = service;
    }

    // GET: Listar todas las categorías
    @GetMapping
    public ResponseEntity<List<BookCategory>> getAllBookCategories() {
        logger.info("Fetching all book categories");
        List<BookCategory> categories = service.getAllBookCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // GET: Obtener categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<BookCategory> getById(@PathVariable long id) throws BookCategoryNotFoundException {
        logger.info("Searching category by ID: {}", id);
        BookCategory category = service.getBookCategoriesById(id);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    // GET: Buscar por nombre
    @GetMapping("/name")
    public ResponseEntity<List<BookCategory>> getByName(@RequestParam String name) {
        logger.info("Searching category by name: {}", name);
        List<BookCategory> categories = service.getBookCategoriesByName(name);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // GET: Buscar por descripción
    @GetMapping("/description")
    public ResponseEntity<List<BookCategory>> getByDescription(@RequestParam String description) {
        logger.info("Searching category by description: {}", description);
        List<BookCategory> categories = service.getBookCategoriesByDescription(description);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // GET: Buscar por estado activo/inactivo
    @GetMapping("/active")
    public ResponseEntity<List<BookCategory>> getByActive(@RequestParam(defaultValue = "true") boolean active) {
        logger.info("Searching categories by active: {}", active);
        List<BookCategory> categories = service.getBookCategoriesByActive(active);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // GET: Buscar por fecha de creación
    @GetMapping("/creation-date")
    public ResponseEntity<List<BookCategory>> getByCreatedDate(@RequestParam LocalDate date) {
        logger.info("Searching categories by creation date: {}", date);
        List<BookCategory> categories = service.getBookCategoriesByCreateDate(date);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // GET: Buscar por número exacto de libros
    @GetMapping("/number-books")
    public ResponseEntity<List<BookCategory>> getByNumberBooks(@RequestParam int numberBooks) {
        logger.info("Searching categories with number of books: {}", numberBooks);
        List<BookCategory> categories = service.getBookCategoriesByNumberBooks(numberBooks);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // GET: Buscar por número mínimo de libros
    @GetMapping("/min-books")
    public ResponseEntity<List<BookCategory>> getWithMinBooks(@RequestParam int minBooks) {
        logger.info("Searching categories with at least {} books", minBooks);
        List<BookCategory> categories = service.getBookCategoriesWithMinBooks(minBooks);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // POST: Crear nueva categoría
    @PostMapping
    public ResponseEntity<BookCategory> addCategory(@Valid @RequestBody BookCategory bookCategory) {
        logger.info("Adding new category: {}", bookCategory.getName());
        BookCategory newCategory = service.saveBookCategory(bookCategory);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    // PUT: Actualizar categoría por ID
    @PutMapping("/{id}")
    public ResponseEntity<BookCategory> updateCategory(@PathVariable long id, @Valid @RequestBody BookCategory details)
            throws BookCategoryNotFoundException {
        logger.info("Updating category ID: {}", id);
        BookCategory updated = service.updateBookCategory(id, details);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    // PATCH: Actualización parcial
    @PatchMapping("/{id}")
    public ResponseEntity<BookCategory> updatePartial(@PathVariable Long id, @RequestBody Map<String, Object> updates)
            throws BookCategoryNotFoundException {
        logger.info("Partially updating category ID: {}", id);
        BookCategory updated = service.updateBookCategoryPartial(id, updates);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    // DELETE: Eliminar por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) throws BookCategoryNotFoundException {
        logger.info("Deleting category ID: {}", id);
        service.deleteBookCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
