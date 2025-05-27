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
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //para obtener todos los usuarios
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info(" BEGIN getAllUsers");
        List<User> users = userService.getAllUsers();
        logger.info(" END getAllUsers - Total users fetched: {}", users.size());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    //para obtener un usuario por email
    @GetMapping("/email")
    public ResponseEntity<User> getUserByEmail(@RequestParam("email") String email) {
        logger.info(" BEGIN getUserByEmail - Searching user with email: {}", email);
        User user = userService.getUserByEmail(email);
        logger.info(" END getUserByEmail - user found: {}", user.getId());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    //para obtener un usuario por nombre de usuario
    @GetMapping("/username")
    public ResponseEntity<User> getUserByUsername(@RequestParam("username") String username) {
        logger.info(" BEGIN getUserByUsername - Searching user with username: {}", username);
        User user = userService.getUserByUsername(username);
        logger.info(" END getUserByUsername - user found: {}", user.getId());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    //para obtener usuarios activos
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        logger.info("BEGIN getActiveUsers - Fetching active users");
        List<User> activeUsers = userService.getActiveUsers();

        if (activeUsers.isEmpty()) {
            logger.info("No active users found.");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        logger.info("END getActiveUsers - Total active users fetched: {}", activeUsers.size());
        return new ResponseEntity<>(activeUsers, HttpStatus.OK);
    }


    //para añadir un usuario nuevo
    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        logger.info(" BEGIN addUser - Adding user: {}", user.getEmail());
        User newUser = userService.saveUser(user);
        logger.info(" END addUser - User added with id: {}", newUser.getId());
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    //para actualizar un usuario por id
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        logger.info(" BEGIN updateUser - Updating user with id: {}", id);
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            logger.info(" END updateUser - User updated with id: {}", id);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            logger.error(" Error in updateUser - User not found with id: {}", id, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //para actualizar un usuario por id parcialmente
    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody Map<String, Object> updates) {
        logger.info(" BEGIN updateUserPartial - Partially updating user with id: {}", id);
        User updatedUser = userService.updateUserPartial(id, updates);
        logger.info("END updateUserPartial - User updated with id: {}", id);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    //para eliminar un usuario por id
    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable long id) {
        logger.info(" BEGIN deleteUser - Deleting user with id: {}", id);
        try {
            userService.deleteUser(id);
            logger.info(" END deleteUser - User deleted with id: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserNotFoundException e) {
            logger.error(" Error in deleteUser - User not found with id: {}", id, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //para manejar las excepciones de usuario no encontrado
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<User> handleUserNotFoundException(UserNotFoundException e) {
        logger.error("Handling UserNotFoundException - {}", e.getMessage());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
