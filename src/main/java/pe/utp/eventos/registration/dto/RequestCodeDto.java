package pe.utp.eventos.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestCodeDto(
        @NotBlank
        @Email
        String email
) {}
