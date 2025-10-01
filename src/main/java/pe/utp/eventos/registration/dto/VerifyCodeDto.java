package pe.utp.eventos.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyCodeDto(
        @NotBlank @Email String email,
        @NotBlank String code
) {}
