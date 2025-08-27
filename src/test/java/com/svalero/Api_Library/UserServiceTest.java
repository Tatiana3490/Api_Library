package com.svalero.Api_Library;

import com.svalero.Api_Library.DTO.UserInDto;
import com.svalero.Api_Library.domain.User;
import com.svalero.Api_Library.exception.UserNotFoundException;
import com.svalero.Api_Library.repository.UserRepository;
import com.svalero.Api_Library.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de UserService con Mockito.
 * Sin BD: todo mockeado.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ModelMapper modelMapper;

    // OJO: lo creamos a mano en @BeforeEach para inyectar los @Autowired de campo
    private UserService userService;

    @BeforeEach
    void setUp() {
        // El servicio solo pide el repo por constructor
        userService = new UserService(userRepository);
        // Pero passwordEncoder y modelMapper son @Autowired por campo → los ponemos con reflexión
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(userService, "modelMapper", modelMapper);
    }

    // ===== Helpers =====
    private User u(Long id, String name, String username, String email,
                   String password, LocalDate creationDate, Boolean active) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setCreationDate(creationDate);
        user.setActive(active);
        return user;
    }

    private UserInDto inDto(String name, String username, String email,
                            String password, LocalDate creationDate, Boolean active) {
        return new UserInDto(name, username, email, password, creationDate, active);
    }

    // ===================== READ =====================

    @Test
    @DisplayName("getAllUsers devuelve la lista completa")
    void getAllUsers_returnsAll() {
        when(userRepository.findAll()).thenReturn(List.of(
                u(1L, "Alice", "alice", "a@x.com", "pass", LocalDate.parse("2024-01-01"), true),
                u(2L, "Bob", "bob", "b@x.com", "pass", LocalDate.parse("2024-01-02"), false)
        ));

        List<User> out = userService.getAllUsers();

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getUsername()).isEqualTo("alice");
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByEmail delega en repo y devuelve el usuario")
    void getUserByEmail_found() {
        when(userRepository.findByEmail("z@x.com"))
                .thenReturn(u(3L, "Zoe", "zoe", "z@x.com", "p", LocalDate.parse("2024-01-03"), true));

        User out = userService.getUserByEmail("z@x.com");

        assertThat(out.getId()).isEqualTo(3L);
        assertThat(out.getEmail()).isEqualTo("z@x.com");
        verify(userRepository).findByEmail("z@x.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByUsername devuelve usuario cuando existe")
    void getUserByUsername_found() {
        when(userRepository.findByUsername("neo"))
                .thenReturn(Optional.of(u(10L, "Neo", "neo", "neo@mx", "p", LocalDate.parse("1999-03-31"), true)));

        User out = userService.getUserByUsername("neo");

        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getUsername()).isEqualTo("neo");
        verify(userRepository).findByUsername("neo");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByUsername lanza UsernameNotFoundException cuando NO existe")
    void getUserByUsername_notFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserByUsername("ghost"));

        verify(userRepository).findByUsername("ghost");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("getActiveUsers devuelve sólo activos (delegado)")
    void getActiveUsers_delegates() {
        when(userRepository.findByActiveTrue()).thenReturn(List.of(
                u(1L, "Act", "act", "act@x", "p", LocalDate.now(), true)
        ));

        List<User> out = userService.getActiveUsers();

        assertThat(out).hasSize(1);
        assertThat(out.get(0).getActive()).isTrue();
        verify(userRepository).findByActiveTrue();
        verifyNoMoreInteractions(userRepository);
    }

    // ===================== CREATE =====================

    @Test
    @DisplayName("saveUser: encripta password, mapea DTO→User y guarda")
    void saveUser_encodes_and_maps_and_saves() {
        UserInDto dto = inDto("Trinity", "trinity", "t@mx", "plainSecret",
                LocalDate.parse("2024-02-02"), true);

        User mapped = u(null, "Trinity", "trinity", "t@mx",
                "ENCODED", LocalDate.parse("2024-02-02"), true);
        User saved = u(99L, "Trinity", "trinity", "t@mx",
                "ENCODED", LocalDate.parse("2024-02-02"), true);

        when(passwordEncoder.encode("plainSecret")).thenReturn("ENCODED");
        when(modelMapper.map(dto, User.class)).thenReturn(mapped);
        when(userRepository.save(mapped)).thenReturn(saved);

        User out = userService.saveUser(dto);

        assertThat(out.getId()).isEqualTo(99L);
        assertThat(out.getPassword()).isEqualTo("ENCODED");
        verify(passwordEncoder).encode("plainSecret");
        verify(modelMapper).map(dto, User.class);
        verify(userRepository).save(mapped);
        verifyNoMoreInteractions(userRepository, passwordEncoder, modelMapper);
    }

    // ===================== UPDATE (PUT) =====================

    @Test
    @DisplayName("updateUser actualiza campos y guarda cuando existe")
    void updateUser_success() throws UserNotFoundException {
        User existing = u(5L, "Old", "olduser", "old@x", "oldpass",
                LocalDate.parse("2023-01-01"), true);
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User changes = u(null, "New Name", "ignoredUsername", "new@x", "newpass",
                LocalDate.parse("2077-12-12"), false);

        User out = userService.updateUser(5L, changes);

        assertThat(out.getId()).isEqualTo(5L);
        assertThat(out.getName()).isEqualTo("New Name");
        assertThat(out.getEmail()).isEqualTo("new@x");
        assertThat(out.getPassword()).isEqualTo("newpass");
        assertThat(out.getActive()).isFalse();
        assertThat(out.getUsername()).isEqualTo("olduser");
        assertThat(out.getCreationDate()).isEqualTo(LocalDate.parse("2023-01-01"));

        verify(userRepository).findById(5L);
        verify(userRepository).save(existing);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("updateUser lanza UserNotFoundException cuando NO existe")
    void updateUser_notFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(404L, u(null, "x", "y", "z", "p", LocalDate.now(), true)));

        verify(userRepository).findById(404L);
        verifyNoMoreInteractions(userRepository);
    }

    // ===================== PATCH =====================

    @Test
    @DisplayName("updateUserPartial aplica conversiones (String→LocalDate, String→Boolean) y guarda")
    void patch_success_with_conversions() {
        User existing = u(7L, "Ana", "ana", "ana@x", "pwd",
                LocalDate.parse("2020-05-05"), true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Ana Actualizada");
        updates.put("active", "false");
        updates.put("creationDate", "2024-10-15");

        User out = userService.updateUserPartial(7L, updates);

        assertThat(out.getName()).isEqualTo("Ana Actualizada");
        assertThat(out.getActive()).isFalse();
        assertThat(out.getCreationDate()).isEqualTo(LocalDate.parse("2024-10-15"));
        assertThat(out.getUsername()).isEqualTo("ana");
        assertThat(out.getEmail()).isEqualTo("ana@x");

        verify(userRepository).findById(7L);
        verify(userRepository).save(existing);
        verifyNoMoreInteractions(userRepository);
    }

    // ===================== DELETE =====================

    @Test
    @DisplayName("deleteUser elimina cuando existe")
    void delete_success() throws UserNotFoundException {
        when(userRepository.existsById(11L)).thenReturn(true);

        userService.deleteUser(11L);

        verify(userRepository).existsById(11L);
        verify(userRepository).deleteById(11L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("deleteUser lanza UserNotFoundException cuando NO existe")
    void delete_notFound() {
        when(userRepository.existsById(12L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(12L));

        verify(userRepository).existsById(12L);
        verify(userRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    // ===================== QUERY NATIVA =====================

    @Test
    @DisplayName("findUsersByNameContaining delega en repo nativo")
    void nameContaining_native_delegates() {
        when(userRepository.findUsersByNameContainingNative("an"))
                .thenReturn(List.of(u(1L, "Ana", "ana", "a@x", "p", LocalDate.now(), true)));

        List<User> out = userService.findUsersByNameContaining("an");

        assertThat(out).hasSize(1);
        assertThat(out.get(0).getName()).isEqualTo("Ana");
        verify(userRepository).findUsersByNameContainingNative("an");
        verifyNoMoreInteractions(userRepository);
    }
}
