package pe.utp.eventos.registration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.eventos.registration.domain.User;
import pe.utp.eventos.registration.repo.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String email, String rawPassword,
                           String nombre, String direccion, String celular) {
        var now = Instant.now();
        var user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .nombre(nombre)
                .direccion(direccion)
                .celular(celular)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return userRepository.save(user);
    }
    @Transactional
    public void updatePassword(String email, String rawPassword) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }
}
