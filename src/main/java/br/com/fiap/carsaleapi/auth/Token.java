package br.com.fiap.carsaleapi.auth;

public record Token(String token, String name, String id, String role, long expiresIn) {
}
