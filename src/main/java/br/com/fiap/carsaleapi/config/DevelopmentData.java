package br.com.fiap.carsaleapi.config;

import br.com.fiap.carsaleapi.domain.user.User;
import br.com.fiap.carsaleapi.domain.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Component
@Profile("dev & !prod")
public class DevelopmentData implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final Environment environment;

    public DevelopmentData(UserRepository users, PasswordEncoder encoder, Environment environment) {
        this.users = users;
        this.encoder = encoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createUser("admin", "Administrador", "ROLE_ADMIN", "CARSALE_ADMIN_PASSWORD");
        createUser("usuario", "Usuário", "ROLE_USER", "CARSALE_USER_PASSWORD");
    }

    private void createUser(String username, String name, String role, String property) {
        if (users.findByUsername(username).isPresent()) {
            return;
        }
        String password = environment.getProperty(property);
        boolean generated = password == null || password.isBlank();
        if (generated) {
            byte[] random = new byte[18];
            new SecureRandom().nextBytes(random);
            password = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        }
        if (password.length() < 12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("A senha de demonstração deve ter no mínimo 12 caracteres e no máximo 72 bytes.");
        }
        var user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setRole(role);
        user.setPassword(encoder.encode(password));
        users.save(user);
        if (generated) {
            System.out.printf("CARSALE DEV | usuário=%s | senha temporária=%s%n", username, password);
        }
    }
}
