package pe.utp.eventos.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetNewPasswordDto(
        @NotBlank String actionToken,
        @NotBlank @Size(min = 8) String newPassword
) {}
