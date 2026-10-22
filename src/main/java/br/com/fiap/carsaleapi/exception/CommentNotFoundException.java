package br.com.fiap.carsaleapi.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(Long id) {
        super("Comment not found with id: " + id);
    }
}
