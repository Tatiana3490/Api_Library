package com.svalero.Api_Library.service;

import com.svalero.Api_Library.DTO.UserInDto;
import com.svalero.Api_Library.domain.User;
import com.svalero.Api_Library.exception.UserNotFoundException;
import com.svalero.Api_Library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.modelmapper.ModelMapper;

import java.lang.reflect.Field;
import java.time.LocalDate;
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
    @Autowired
    private ModelMapper modelMapper;

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
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    //para obtener los usuarios activos
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    //para guardar un nuevo usuario
    public User saveUser(UserInDto user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User newUser= modelMapper.map(user, User.class);
        return userRepository.save(newUser);
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
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(User.class, key);
            if (field == null) return;

            field.setAccessible(true);
            Object toSet = value;
            Class<?> type = field.getType();

            if (value != null) {
                if (type.equals(LocalDate.class)) {
                    toSet = LocalDate.parse(value.toString()); // "yyyy-MM-dd"
                } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                    toSet = (value instanceof Boolean) ? value : Boolean.parseBoolean(value.toString());
                } else if (type.equals(Long.class) || type.equals(long.class)) {
                    toSet = (value instanceof Number) ? ((Number) value).longValue() : Long.parseLong(value.toString());
                } else if (type.equals(Integer.class) || type.equals(int.class)) {
                    toSet = (value instanceof Number) ? ((Number) value).intValue() : Integer.parseInt(value.toString());
                }
                // añade más conversiones si tienes otros tipos (Double, etc.)
            }

            ReflectionUtils.setField(field, user, toSet);
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

    // ===================== CONSULTAS SQL =====================
    public List<User> findUsersByNameContaining(String keyword) {
        return userRepository.findUsersByNameContainingNative(keyword);
    }



}
