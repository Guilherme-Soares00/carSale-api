package br.com.fiap.carsaleapi.domain.comments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties("userId")
public record CommentRequest(
        @NotBlank(message = "Informe o comentário.")
        @Size(max = 2000, message = "O comentário deve ter no máximo 2000 caracteres.") String comment,
        @NotNull(message = "Informe o carro.") @Positive(message = "O carro deve ter ID positivo.") Long carId
) {
    public CommentRequest {
        comment = comment == null ? null : comment.strip();
    }
}
