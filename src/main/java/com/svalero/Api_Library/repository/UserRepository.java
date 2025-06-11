package com.svalero.Api_Library.repository;

import com.svalero.Api_Library.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    // ================= BÚSQUEDAS BÁSICAS ================= //
    //Metodos para buscar
    List<User> findAll();
    User findByEmail(String email);
    User findByUsername(String username);
    List<User> findByActiveTrue();

}
