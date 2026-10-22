package br.com.fiap.carsaleapi.exception;

public record ApiError(int status, String message) {
}
