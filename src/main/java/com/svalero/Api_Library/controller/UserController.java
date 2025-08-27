package com.svalero.Api_Library.controller;


import com.svalero.Api_Library.domain.User;
import com.svalero.Api_Library.exception.UserNotFoundException;
import com.svalero.Api_Library.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    // ========== GET: Listar todos los usuarios ==========
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = service.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // ========== GET: Obtener usuario por email ==========
    @GetMapping("/email")
    public ResponseEntity<User> getByEmail(@RequestParam String email) {
        logger.info("Searching user by email: {}", email);
        User user = service.getUserByEmail(email);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // ========== GET: Obtener usuario por username ==========
    @GetMapping("/username")
    public ResponseEntity<User> getByUsername(@RequestParam String username) {
        logger.info("Searching user by username: {}", username);
        User user = service.getUserByUsername(username);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // ========== GET: Usuarios activos ==========
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        logger.info("Fetching active users");
        List<User> users = service.getActiveUsers();
        return users.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(users, HttpStatus.OK);
    }

    // ========== POST: Crear nuevo usuario ==========
    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User dto) {
        logger.info("Adding new user: {}", dto.getEmail());
        User newUser = service.saveUser(dto); // ← ahora le pasamos el DTO correcto
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // ========== PUT: Actualización completa ==========
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @Valid @RequestBody User dto)
            throws UserNotFoundException {
        logger.info("Updating user with ID: {}", id);
        User updatedUser = service.updateUser(id, dto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    // ========== PATCH: Actualización parcial ==========
    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUserPartial(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> updates) {
        logger.info("Partially updating user with ID: {}", id);
        User updatedUser = service.updateUserPartial(id, updates);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    // ========== DELETE: Eliminar usuario ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) throws UserNotFoundException {
        logger.info("Deleting user with ID: {}", id);
        service.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ========== GET (consulta “search”) ==========
    @GetMapping("/search")
    public ResponseEntity<List<User>> getUsersByNameContaining(@RequestParam String name) {
        // 🧹 He quitado el segundo UserService duplicado; usamos el mismo 'service'
        List<User> users = service.findUsersByNameContaining(name);
        return ResponseEntity.ok(users);
    }
}
