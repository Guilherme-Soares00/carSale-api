package br.com.fiap.carsaleapi.domain.car;

import br.com.fiap.carsaleapi.domain.comments.Comment;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cars")
public class Car {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String model;
    @Column(nullable = false, length = 80)
    private String brand;
    @Column(nullable = false, length = 2000)
    private String description;
    @Column(nullable = false, length = 1000)
    private String imageUrl;
    @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
    @OrderBy("createdDate DESC, id DESC")
    private List<Comment> comments = new ArrayList<>();

    public Long getId() { return id; }
    public String getModel() { return model; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public List<Comment> getComments() { return comments; }
}
