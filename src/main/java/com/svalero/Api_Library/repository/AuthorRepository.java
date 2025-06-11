package com.svalero.Api_Library.repository;

import com.svalero.Api_Library.domain.Author;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AuthorRepository extends CrudRepository<Author, Long> {

    // ================= BÚSQUEDAS BÁSICAS ================= //
    //Metodos para buscar
    List<Author> findAll();
    List<Author> findByName(String name);
    List<Author> findBySurname(String surname);
    List<Author> findByNationality(String nationality);
    List<Author> findByBirthdate(LocalDate birthdate);

}
