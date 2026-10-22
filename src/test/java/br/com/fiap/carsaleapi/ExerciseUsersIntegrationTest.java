package br.com.fiap.carsaleapi;

import br.com.fiap.carsaleapi.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:exercise-users;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@ActiveProfiles("dev")
class ExerciseUsersIntegrationTest {

    @Autowired private UserRepository users;

    @Test
    void loadsOriginalExerciseAccountsWithTheirExistingPasswordHashes() {
        assertThat(users.findAll())
                .extracting("name", "username", "role", "password")
                .containsExactlyInAnyOrder(
                        tuple("Alice Smith", "alice.smith", "ROLE_ADMIN", "$2a$12$h3cu/Kp00GspyxPqq9GOjeM0ZaY2679ESM50m8ohPsHClhN.YrQTa"),
                        tuple("Bob Johnson", "bob.johnson", "ROLE_USER", "$2a$12$bY8yIX2ahPwy.ADZciIs7OKV7LPzDktzirb521OtTv8cch4Ta6WaK"),
                        tuple("Charlie Brown", "charlie.brown", "ROLE_USER", "$2a$12$MO7apjxKdvmPlCe0Q3Vv9.ceEwk877lj25c/q6EaJZk1whnyV/H9W"));
    }
}
