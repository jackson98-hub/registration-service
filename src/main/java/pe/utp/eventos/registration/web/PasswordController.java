package pe.utp.eventos.registration.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.utp.eventos.registration.domain.enums.VerificationPurpose;
import pe.utp.eventos.registration.dto.*;
import pe.utp.eventos.registration.repo.UserRepository;
import pe.utp.eventos.registration.service.JwtService;
import pe.utp.eventos.registration.service.VerificationService;
import pe.utp.eventos.registration.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import pe.utp.eventos.registration.dto.CodeOnlyDto;
import java.util.Map;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordController {

    private final VerificationService verificationService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userService;

    // Enviar código + enlace (no revelamos si el email existe)
    @PostMapping("/request-code")
    public ResponseEntity<ApiMessage> requestCode(@Valid @RequestBody RequestCodeDto dto) {
        userRepository.findByEmail(dto.email()).ifPresent(u ->
                verificationService.createAndSend(dto.email(), VerificationPurpose.RESET_PASSWORD)
        );
        return ResponseEntity.ok(new ApiMessage("Si el correo existe, se envió un código"));
    }

    // Verificar código -> entregar actionToken corto
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyCodeDto dto) {
        boolean ok = verificationService.validate(dto.email(), VerificationPurpose.RESET_PASSWORD, dto.code());
        if (!ok) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Código inválido o expirado"));
        }
        String token = jwtService.generateActionToken(dto.email(), "RESET_PASSWORD", 10);
        return ResponseEntity.ok(new ActionTokenResponse(token, 600));
    }

    // Establecer nueva contraseña usando actionToken
    @PostMapping("/set")
    public ResponseEntity<ApiMessage> setNewPassword(@Valid @RequestBody SetNewPasswordDto dto) {
        String email = jwtService.parseAndValidate(dto.actionToken(), "RESET_PASSWORD");
        userService.updatePassword(email, dto.newPassword());
        return ResponseEntity.ok(new ApiMessage("Contraseña actualizada"));
    }

    private String requireEmailFromAccessToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta Authorization: Bearer token");
        }
        String token = header.substring(7);
        try {
            return jwtService.parseAndValidateAccess(token);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        }
    }

    /** 1) Enviar código al email del usuario autenticado */
    @PostMapping("/change/request-code")
    public ResponseEntity<ApiMessage> changeRequestCode(HttpServletRequest req) {
        String email = requireEmailFromAccessToken(req);
        verificationService.createAndSend(email, VerificationPurpose.CHANGE_PASSWORD);
        return ResponseEntity.ok(new ApiMessage("Código enviado"));
    }

    /** 2) Verificar código -> emitir actionToken (purpose=CHANGE_PASSWORD) */
    @PostMapping("/change/verify")
    public ResponseEntity<?> changeVerify(HttpServletRequest req,
                                          @Valid @RequestBody CodeOnlyDto dto) {
        String email = requireEmailFromAccessToken(req);
        boolean ok = verificationService.validate(email, VerificationPurpose.CHANGE_PASSWORD, dto.code());
        if (!ok) {
            return ResponseEntity.badRequest().body(new ApiMessage("Código inválido o expirado"));
        }
        String token = jwtService.generateActionToken(email, "CHANGE_PASSWORD", 10); // 10 min
        return ResponseEntity.ok(new ActionTokenResponse(token, 600));
    }

    /** 3) Confirmar cambio con actionToken + nueva contraseña */
    @PostMapping("/change/set")
    public ResponseEntity<ApiMessage> changeSet(@Valid @RequestBody SetNewPasswordDto dto) {
        String email = jwtService.parseAndValidate(dto.actionToken(), "CHANGE_PASSWORD");
        userService.updatePassword(email, dto.newPassword());
        return ResponseEntity.ok(new ApiMessage("Contraseña actualizada"));
    }
}
