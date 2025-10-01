package pe.utp.eventos.registration.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.eventos.registration.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
