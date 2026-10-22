package br.com.fiap.carsaleapi.domain.comments;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentsController {
    private final CommentService comments;

    public CommentsController(CommentService comments) {
        this.comments = comments;
    }

    @GetMapping
    public List<CommentResponse> getAllComments() {
        return comments.getAllComments();
    }

    @GetMapping("/{id}")
    public CommentResponse getCommentById(@PathVariable @Positive Long id) {
        return comments.getCommentById(id);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@RequestBody @Valid CommentRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        var comment = comments.createComment(request, jwt.getSubject());
        return ResponseEntity.created(URI.create("/comments/" + comment.id())).body(comment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable @Positive Long id) {
        comments.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
