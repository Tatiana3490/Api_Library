package com.svalero.Api_Library.service;

import com.svalero.Api_Library.domain.User;
import com.svalero.Api_Library.exception.UserNotFoundException;
import com.svalero.Api_Library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    private PasswordEncoder passwordEncoder;

    //para obtener todos los usuarios
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //para obtener usuario por email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //para obtener usuarios por nombre de usuario
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    //para obtener los usuarios activos
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    //para guardar un nuevo usuario
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }


    // Actualizar un usuario por ID
    public User updateUser(Long id, User userDetails) throws UserNotFoundException {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Para actualizar los campos del usuario existente con los nuevos valores
        existingUser.setName(userDetails.getName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setPassword(userDetails.getPassword());
        existingUser.setActive(userDetails.getActive());

        return userRepository.save(existingUser);
    }

    public User updateUserPartial(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(User.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, user, value);
            }
        });

        return userRepository.save(user);
    }

    // para eliminar un usuario por id
    public void deleteUser(long id) throws UserNotFoundException {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
