package com.svalero.Api_Library.security;

import com.svalero.Api_Library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User.UserBuilder;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.svalero.Api_Library.domain.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Usamos el builder de Spring Security
        UserBuilder builder = User.builder();
        builder.username(user.getUsername());
        builder.password(user.getPassword());
        builder.roles("USER");

        return builder.build();
    }
}
