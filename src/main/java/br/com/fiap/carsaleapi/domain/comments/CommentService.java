package br.com.fiap.carsaleapi.domain.comments;

import br.com.fiap.carsaleapi.domain.car.CarRepository;
import br.com.fiap.carsaleapi.domain.user.UserRepository;
import br.com.fiap.carsaleapi.exception.CarNotFoundException;
import br.com.fiap.carsaleapi.exception.CommentNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository comments;
    private final CarRepository cars;
    private final UserRepository users;
    private final Clock clock;

    public CommentService(CommentRepository comments, CarRepository cars, UserRepository users, Clock clock) {
        this.comments = comments;
        this.cars = cars;
        this.users = users;
        this.clock = clock;
    }

    public List<CommentResponse> getAllComments() {
        return comments.findAll().stream().map(CommentResponse::fromModel).toList();
    }

    public CommentResponse getCommentById(Long id) {
        return CommentResponse.fromModel(comments.findById(id).orElseThrow(() -> new CommentNotFoundException(id)));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CommentResponse createComment(CommentRequest request, String username) {
        var car = cars.findById(request.carId()).orElseThrow(() -> new CarNotFoundException(request.carId()));
        var user = users.findByUsername(username).orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));
        var comment = comments.save(new Comment(request.comment(), LocalDate.now(clock), car, user));
        return CommentResponse.fromModel(comment);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteComment(Long id) {
        var comment = comments.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        comments.delete(comment);
    }
}
