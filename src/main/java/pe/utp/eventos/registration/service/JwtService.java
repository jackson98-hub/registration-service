package pe.utp.eventos.registration.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")     // Debe ser Base64 (32 bytes = 256 bits)
    private String secret;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.access-exp-min}")
    private long accessExpMin;

    private SecretKey key;

    @PostConstruct
    void init() {
        // secret esperado en Base64 (ej.: openssl rand -base64 32)
        byte[] raw = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(raw);  // SecretKey válido para HS256/HS512
    }

    /** Token de acción de corta vida (para flujos: REGISTER / RESET_PASSWORD). */
    public String generateActionToken(String email, String purpose, long expMinutes) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expMinutes * 60);

        return Jwts.builder()
                .subject(email)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(Map.of(
                        "type", "action",
                        "purpose", purpose
                ))
                .signWith(key)  // HS256 por defecto con esta key
                .compact();
    }

    /** Valida firma/expiración y que sea un token de acción con el purpose esperado. */
    public String parseAndValidate(String token, String expectedPurpose) {
        var jwt = Jwts.parser()
                .verifyWith(key)   // requiere SecretKey en jjwt 0.12.x
                .build()
                .parseSignedClaims(token);

        var claims = jwt.getPayload();

        Object type = claims.get("type");
        if (!"action".equals(type)) {
            throw new IllegalStateException("Invalid token type");
        }

        Object purpose = claims.get("purpose");
        if (!expectedPurpose.equals(purpose)) {
            throw new IllegalStateException("Invalid purpose");
        }

        return claims.getSubject(); // retorna el email
    }

    /** Útil si quieres reportar el tiempo de vida configurado (en segundos). */
    public long getAccessExpSeconds() {
        return accessExpMin * 60;
    }

    public String parseAndValidateAccess(String token) {
        var jwt = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);

        var claims = jwt.getPayload();
        Object type = claims.get("type");
        if (!"access".equals(type)) {
            throw new IllegalStateException("Invalid token type");
        }
        return claims.getSubject(); // email del usuario autenticado
    }

}
