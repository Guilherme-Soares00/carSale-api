package br.com.fiap.carsaleapi.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({CarNotFoundException.class, CommentNotFoundException.class})
    public ResponseEntity<ApiError> notFound(RuntimeException exception) {
        return error(404, exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> unauthenticated(AuthenticationException exception) {
        return error(401, "Usuário ou senha inválidos.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> forbidden(AccessDeniedException exception) {
        return error(403, "Acesso não permitido.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception) {
        return error(400, "Dados inválidos. Verifique os campos e os limites informados.");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiError> invalidInput(Exception exception) {
        return error(400, "Requisição inválida. Verifique o JSON e os identificadores.");
    }

    private ResponseEntity<ApiError> error(int status, String message) {
        return ResponseEntity.status(status).body(new ApiError(status, message));
    }
}
