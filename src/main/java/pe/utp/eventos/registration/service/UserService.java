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

        // Obtener el último ID
        String lastId = userRepository.findLastUserId().orElse("CLI-000");

        // Extraer parte numérica y sumar 1
        long nextNumber = Long.parseLong(lastId.substring(4)) + 1;

        // Formatear el nuevo ID con ceros
        String newId = String.format("CLI-%016d", nextNumber);

        var now = Instant.now();

        var user = User.builder()
                .id(newId)
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
