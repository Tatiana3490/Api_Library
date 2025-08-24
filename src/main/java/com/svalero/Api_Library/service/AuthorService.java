package com.svalero.Api_Library.service;

import com.svalero.Api_Library.domain.Author;
import com.svalero.Api_Library.exception.AuthorNotFoundException;
import com.svalero.Api_Library.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }


    //Para obtener todos los autores
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

       //para obtener autores por nombre
    public List<Author> getAuthorByName(String name) {
        return authorRepository.findByName(name);
    }

    //Para obtener autores por apellido (surname)
    public List<Author> getAuthorBySurname(String surname) {
        return authorRepository.findBySurname(surname);
    }

    //Para obtener autores por nacionalidad (nationality)
    public List<Author> getAuthorByNationality(String nationality) {
        return authorRepository.findByNationality(nationality);
    }

    //para obtener autores por fecha de nacimiento (birthdate)
    public List<Author> getAuthorByBirthdate(LocalDate birthdate) {
        return authorRepository.findByBirthdate(birthdate);
    }

    //para obtener un autor por id
    public Author getAuthorById(long id) throws AuthorNotFoundException {
        return authorRepository.findById(id)
                .orElseThrow(() ->new RuntimeException("Book Category not found with id: "+ id));
    }

    //Para guardar un nuevo autor
    public Author saveAuthor(Author author) {
        return authorRepository.save(author);
    }

    //Para eliminar un author por id
    public void deleteAuthor(long id) throws AuthorNotFoundException {
        if (!authorRepository.existsById(id)){
            throw new AuthorNotFoundException("Author not found with id: " + id);
        }
        authorRepository.deleteById(id);
    }

    //Para actualizar un autor por id
    public Author updateAuthor(long id, Author authorDetails) throws AuthorNotFoundException {
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found with id: " + id));

        //Para actualizar los campos del autor existente con los nuevos valores
        existingAuthor.setName(authorDetails.getName());
        existingAuthor.setSurname(authorDetails.getSurname());
        existingAuthor.setBirthdate(authorDetails.getBirthdate());
        existingAuthor.setNationality(authorDetails.getNationality());
        existingAuthor.setActive(authorDetails.getActive());
        existingAuthor.setLatitude(authorDetails.getLatitude());
        existingAuthor.setLongitude(authorDetails.getLongitude());

        return authorRepository.save(existingAuthor);
    }

    public Author updateAuthorPartial(long id, Map<String,Object> updates){
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));


        updates.forEach((key, value) ->{
            Field field = ReflectionUtils.findField(Author.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, author, value);
            }
        });



        return authorRepository.save(author);

    }

}
