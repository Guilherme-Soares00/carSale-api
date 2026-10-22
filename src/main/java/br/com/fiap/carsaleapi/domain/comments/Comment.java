package br.com.fiap.carsaleapi.domain.comments;

import br.com.fiap.carsaleapi.domain.car.Car;
import br.com.fiap.carsaleapi.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "comments", indexes = @Index(name = "idx_comments_car", columnList = "car_id"))
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 2000)
    private String comment;
    @Column(nullable = false)
    private LocalDate createdDate;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Comment() {
    }

    public Comment(String comment, LocalDate createdDate, Car car, User user) {
        this.comment = comment;
        this.createdDate = createdDate;
        this.car = car;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getComment() { return comment; }
    public LocalDate getCreatedDate() { return createdDate; }
    public Car getCar() { return car; }
    public User getUser() { return user; }
}
