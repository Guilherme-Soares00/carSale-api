package br.com.fiap.carsaleapi.domain.car;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    @Override
    @EntityGraph(attributePaths = {"comments", "comments.user"})
    List<Car> findAll();

    @Override
    @EntityGraph(attributePaths = {"comments", "comments.user"})
    Optional<Car> findById(Long id);
}
