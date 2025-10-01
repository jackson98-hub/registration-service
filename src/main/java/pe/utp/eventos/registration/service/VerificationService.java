package pe.utp.eventos.registration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.utp.eventos.registration.domain.enums.VerificationPurpose;
import pe.utp.eventos.registration.service.mail.EmailSender;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final InMemoryCodeStore store;
    private final EmailSender emailSender;

    @Value("${app.reset.frontend-url}")
    private String resetFrontendUrl;

    public void createAndSend(String email, VerificationPurpose purpose) {
        String code = CodeGenerator.numeric6();
        store.save(email, purpose, code);

        StringBuilder body = new StringBuilder();
        body.append("Hola, tu código es: ").append(code)
                .append("\nCaduca en 10 minutos.");

        if (purpose == VerificationPurpose.RESET_PASSWORD) {
            String link = resetFrontendUrl + "?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
            body.append("\n\nSi prefieres, abre este enlace y sigue las instrucciones:\n")
                    .append(link);
        }

        emailSender.send(email,
                (purpose == VerificationPurpose.RESET_PASSWORD) ? "Reestablecer contraseña" : "Código de verificación",
                body.toString());
    }

    public boolean validate(String email, VerificationPurpose purpose, String code) {
        return store.validate(email, purpose, code);
    }
}
