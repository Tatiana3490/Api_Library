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

    //Obtener todos los autores
    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        logger.info("BEGIN getAllAuthors");
        List<Author> authors = authorService.getAllAuthors();
        logger.info("END getAllAuthors - Total authors fetched: {}", + authors.size());
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    //Añadir un nuevo autor
    @PostMapping
    public ResponseEntity<Author> addAuthor(@Valid @RequestBody Author author) {
        logger.info("BEGIN addAuthor - Adding new author: {}", author.getName());
        Author newAuthor = authorService.saveAuthor(author);
        logger.info("END addAuthor - Author added with id: {}", newAuthor.getId());
        return new ResponseEntity<>(newAuthor, HttpStatus.CREATED);
    }

    //Buscar por nombre de autor
    @GetMapping("/name")
    public ResponseEntity<List<Author>> getAuthorByName(@RequestParam String name) {
        logger.info("BEGIN getAuthorByName  - Searching author by name: {}", name);
        List<Author> authors = authorService.getAuthorByName(name);
        logger.info("END getAuthorByName - Authors fetched: {}", + authors.size());
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    //Buscar por apellido del autor
    @GetMapping("/surname")
    public ResponseEntity<List<Author>> getAuthorBySurname(@RequestParam String surname) {
        logger.info("BEGIN getAuthorBySurname  - Searching author by name: {}", surname);
        List<Author> authors = authorService.getAuthorBySurname(surname);
        logger.info("END getAuthorBySurname - Authors fetched: {}", + authors.size());
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }
    //Buscar autores por nacionalidad
    @GetMapping("/nationality")
    public ResponseEntity<List<Author>> getAuthorByNationality(@RequestParam String nationality) {
        logger.info("BEGIN getAuthorByNationality - Searching author by name: {}", nationality);
        List<Author> authors = authorService.getAuthorByNationality(nationality);
        logger.info("END getAuthorByNationality - Authors fetched: {}", + authors.size());
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    //Buscar autores por fecha de nacimiento
    @GetMapping("/birthday")
    public ResponseEntity<List<Author>> getAuthorByBirthday(@RequestParam LocalDate birthday) {
        logger.info("BEGIN getAuthorByBirthday - Searching author by birthday: {}", birthday);
        List<Author> authors = authorService.getAuthorByBirthdate(birthday);
        logger.info("END getAuthorByBirthday - Authors fetched: {}", + authors.size());
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    //Obtener autores por id
    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable long id) throws AuthorNotFoundException {
        logger.info("BEGIN getAuthorById - Searching author by id: {}", id);
        try {
            Author author = authorService.getAuthorById(id);
            logger.info("END getAuthorById - Author fetched: {}", + author.getId());
            return new ResponseEntity<>(author, HttpStatus.OK);
        } catch (Exception e){
            logger.error("Error in getAuthorById - Author not found with id: {}", id,e);
            throw e;
        }
    }

    //Actualizar autor por id
    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable long id, @Valid @RequestBody Author authorDetails)
            throws AuthorNotFoundException {
        logger.info("BEGIN updateAuthor - Updating author by id: {}", id);
        try {
            Author updatedAuthor = authorService.updateAuthor(id, authorDetails);
            logger.info("END updateAuthor - Author updated with id: {}", + updatedAuthor.getId());
            return new ResponseEntity<>(updatedAuthor, HttpStatus.OK);
        }catch (Exception e) {
            logger.error("Error in updateAuthor - Author not found with id: {}", id,e);
            throw e;
        }
    }

    //Actualizar parcialmente un autor por id
    @PatchMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable long id, @Valid @RequestBody Map<String, Object> updates)
            throws AuthorNotFoundException {
        logger.info("BEGIN updateAuthor - Partially Updating author by id: {}", id);
        try {
            Author updateAuthor = authorService.updateAuthorPartial(id, updates);
            logger.info("END updateAuthor - Author updated with id: {}", + updateAuthor.getId());
            return ResponseEntity.ok(updateAuthor);
        } catch (Exception e){
            logger.error("Error in updateAuthor - Author not found with id: {}", id,e);
            throw e;
        }
    }

    //Para eliminar un autor por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable long id) throws AuthorNotFoundException {
        logger.info("BEGIN deleteAuthor - Deleting author by id: {}", id);
        try {
            authorService.deleteAuthor(id);
            logger.info("END deleteAuthor - Author deleted with id: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e){
            logger.error("Error in deleteAuthor - Author not found with id: {}", id,e);
            throw e;
        }
    }

    //Manejar excepciones de autor no encontrado
    @ExceptionHandler(AuthorNotFoundException.class)
    public ResponseEntity<String> handleAuthorNotFoundException(AuthorNotFoundException e) {
        logger.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}
