package br.com.fiap.carsaleapi.domain.comments;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Override
    @EntityGraph(attributePaths = "user")
    List<Comment> findAll();

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<Comment> findById(Long id);
}
