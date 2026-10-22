package br.com.fiap.carsaleapi;

import br.com.fiap.carsaleapi.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:exercise-users;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class ExerciseUsersIntegrationTest {

    @Autowired private UserRepository users;
    @Autowired private TestRestTemplate http;
    @Autowired private MockMvc mvc;

    @Test
    void deniesH2ConsoleFromRemoteAddresses() throws Exception {
        mvc.perform(get("/h2-console/").with(request -> {
            request.setServletPath("/h2-console/");
            request.setRemoteAddr("198.51.100.10");
            return request;
        })).andExpect(status().isForbidden());
    }

    @Test
    void opensH2ConsoleLocallyInDevelopment() {
        var response = http.getForEntity("/h2-console/", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("H2 Console");
        assertThat(response.getHeaders().getFirst("X-Frame-Options")).isEqualTo("SAMEORIGIN");
    }

    @Test
    void loadsExerciseAccountsWithBcryptPasswords() {
        assertThat(users.findAll())
                .extracting("name", "username", "role")
                .containsExactlyInAnyOrder(
                        tuple("Alice Smith", "alice.smith", "ROLE_ADMIN"),
                        tuple("Bob Johnson", "bob.johnson", "ROLE_USER"),
                        tuple("Charlie Brown", "charlie.brown", "ROLE_USER"));
        assertThat(users.findAll()).allSatisfy(user ->
                assertThat(user.getPassword()).matches("\\$2a\\$12\\$[./A-Za-z0-9]{53}"));
    }
}
