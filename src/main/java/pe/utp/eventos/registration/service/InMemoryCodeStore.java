package pe.utp.eventos.registration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.utp.eventos.registration.domain.enums.VerificationPurpose;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryCodeStore {

    private record CodeEntry(String code, Instant expiresAt, int attempts) {}

    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();

    @Value("${app.verification.ttl-minutes}")
    private long ttlMinutes;

    @Value("${app.verification.max-attempts}")
    private int maxAttempts;

    public void save(String email, VerificationPurpose purpose, String code) {
        Instant expires = Instant.now().plusSeconds(ttlMinutes * 60);
        String key = key(email, purpose);
        store.put(key, new CodeEntry(code, expires, 0));
    }

    public boolean validate(String email, VerificationPurpose purpose, String code) {
        String key = key(email, purpose);
        CodeEntry entry = store.get(key);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt())) return false;
        if (entry.attempts() >= maxAttempts) return false;

        boolean ok = entry.code().equals(code);
        // actualizar intentos
        store.put(key, new CodeEntry(entry.code(), entry.expiresAt(), entry.attempts() + 1));
        if (ok) store.remove(key); // si el código es correcto, lo consumimos
        return ok;
    }

    private String key(String email, VerificationPurpose purpose) {
        return purpose.name() + ":" + email;
    }
}
