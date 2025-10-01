package pe.utp.eventos.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteRegistrationDto(
        @NotBlank String actionToken,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String nombre,
        String direccion,
        String celular
) {}
