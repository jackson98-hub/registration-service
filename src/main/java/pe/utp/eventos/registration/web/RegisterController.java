package pe.utp.eventos.registration.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.utp.eventos.registration.domain.enums.VerificationPurpose;
import pe.utp.eventos.registration.dto.*;
import pe.utp.eventos.registration.repo.UserRepository;
import pe.utp.eventos.registration.service.JwtService;
import pe.utp.eventos.registration.service.UserService;
import pe.utp.eventos.registration.service.VerificationService;
import java.util.Map;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {

    private final VerificationService verificationService;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;

    // Endpoint para solicitar código
    @PostMapping("/request-code")
    public ResponseEntity<ApiMessage> requestCode(@Valid @RequestBody RequestCodeDto dto) {
        verificationService.createAndSend(dto.email(), VerificationPurpose.REGISTER);
        return ResponseEntity.ok(new ApiMessage("Código enviado al correo"));
    }

    // Endpoint para verificar código
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyCodeDto dto) {
        boolean ok = verificationService.validate(dto.email(), VerificationPurpose.REGISTER, dto.code());
        if (!ok) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Código inválido o expirado"));
        }

        String token = jwtService.generateActionToken(dto.email(), "REGISTER", 10);
        return ResponseEntity.ok(new ActionTokenResponse(token, 600));
    }

    @PostMapping("/complete")
    public ResponseEntity<ApiMessage> complete(@Valid @RequestBody CompleteRegistrationDto dto) {
        String email = jwtService.parseAndValidate(dto.actionToken(), "REGISTER");
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(new ApiMessage("Email ya registrado"));
        }
        userService.createUser(email, dto.password(), dto.nombre(), dto.direccion(), dto.celular());
        return ResponseEntity.ok(new ApiMessage("Usuario registrado con éxito"));
    }
}
