package br.com.fiap.carsaleapi.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Size(max = 72) String password
) {
    public LoginRequest {
        username = username == null ? null : username.strip();
    }
}
