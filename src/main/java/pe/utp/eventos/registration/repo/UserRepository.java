package pe.utp.eventos.registration.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.utp.eventos.registration.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    // Consulta nativa para obtener el último ID con prefijo "CLI-"
    @Query(value = "SELECT id FROM auth_users WHERE id LIKE 'CLI-%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastUserId();
}
