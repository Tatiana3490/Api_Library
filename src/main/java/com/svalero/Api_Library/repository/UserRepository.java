package com.svalero.Api_Library.repository;

import com.svalero.Api_Library.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    // ================= BÚSQUEDAS BÁSICAS ================= //
    //Metodos para buscar
    List<User> findAll();
    User findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findByActiveTrue();

    // ================= CONSULTAS SQL NATIVAS ================= //
    @Query(value = "SELECT * FROM users WHERE LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%'))", nativeQuery = true)
    List<User> findUsersByNameContainingNative(String keyword);






}
