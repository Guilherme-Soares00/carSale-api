package br.com.fiap.carsaleapi.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, unique = true, length = 80)
    private String username;
    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String password;
    @Column(nullable = false, length = 20)
    private String role;

    public Long getId() { return id; }
    public String getName() { return name; }
    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    public String getRole() { return role; }
    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }
}
