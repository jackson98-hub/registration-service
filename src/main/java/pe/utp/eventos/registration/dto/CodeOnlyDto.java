package pe.utp.eventos.registration.dto;

import jakarta.validation.constraints.NotBlank;

public record CodeOnlyDto(@NotBlank String code) {}
