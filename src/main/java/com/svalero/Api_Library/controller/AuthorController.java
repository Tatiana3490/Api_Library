package com.svalero.Api_Library.controller;

import com.svalero.Api_Library.domain.Author;
import com.svalero.Api_Library.exception.AuthorNotFoundException;
import com.svalero.Api_Library.service.AuthorService;
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
@RequestMapping("/authors")
public class AuthorController {

    private final Logger logger = LoggerFactory.getLogger(AuthorController.class);
    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // GET: Listar todos los autores
    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        logger.info("BEGIN getAllAuthors");
        List<Author> authors = authorService.getAllAuthors();
        logger.info("END getAllAuthors - Total authors fetched: {}", authors.size());
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    // POST: Crear un nuevo autor
    @PostMapping
    public ResponseEntity<Author> addAuthor(@Valid @RequestBody Author author) {
        logger.info("BEGIN addAuthor - Adding new author: {}", author.getName());
        Author newAuthor = authorService.saveAuthor(author);
        logger.info("END addAuthor - Author added with ID: {}", newAuthor.getId());
        return new ResponseEntity<>(newAuthor, HttpStatus.CREATED);
    }

    // GET: Buscar autores por nombre
    @GetMapping("/name")
    public ResponseEntity<List<Author>> getAuthorByName(@RequestParam String name) {
        logger.info("Searching author by name: {}", name);
        List<Author> authors = authorService.getAuthorByName(name);
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    // GET: Buscar autores por apellido
    @GetMapping("/surname")
    public ResponseEntity<List<Author>> getAuthorBySurname(@RequestParam String surname) {
        logger.info("Searching author by surname: {}", surname);
        List<Author> authors = authorService.getAuthorBySurname(surname);
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    // GET: Buscar autores por nacionalidad
    @GetMapping("/nationality")
    public ResponseEntity<List<Author>> getAuthorByNationality(@RequestParam String nationality) {
        logger.info("Searching author by nationality: {}", nationality);
        List<Author> authors = authorService.getAuthorByNationality(nationality);
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    // GET: Buscar autores por fecha de nacimiento
    @GetMapping("/birthday")
    public ResponseEntity<List<Author>> getAuthorByBirthday(@RequestParam LocalDate birthday) {
        logger.info("Searching author by birthday: {}", birthday);
        List<Author> authors = authorService.getAuthorByBirthdate(birthday);
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    // GET: Obtener autor por ID
    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable long id) throws AuthorNotFoundException {
        logger.info("Searching author by ID: {}", id);
        Author author = authorService.getAuthorById(id);
        return new ResponseEntity<>(author, HttpStatus.OK);
    }

    // PUT: Actualizar autor completamente por ID
    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable long id, @Valid @RequestBody Author authorDetails)
            throws AuthorNotFoundException {
        logger.info("Updating author by ID: {}", id);
        Author updatedAuthor = authorService.updateAuthor(id, authorDetails);
        return new ResponseEntity<>(updatedAuthor, HttpStatus.OK);
    }

    // PATCH: Actualizar parcialmente un autor por ID
    @PatchMapping("/{id}")
    public ResponseEntity<Author> updateAuthorPartial(@PathVariable long id, @RequestBody Map<String, Object> updates)
            throws AuthorNotFoundException {
        logger.info("Partially updating author by ID: {}", id);
        Author updatedAuthor = authorService.updateAuthorPartial(id, updates);
        return ResponseEntity.ok(updatedAuthor);
    }

    // DELETE: Eliminar autor por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable long id) throws AuthorNotFoundException {
        logger.info("Deleting author by ID: {}", id);
        authorService.deleteAuthor(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
