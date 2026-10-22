package br.com.fiap.carsaleapi;

import br.com.fiap.carsaleapi.domain.comments.CommentRepository;
import br.com.fiap.carsaleapi.domain.user.User;
import br.com.fiap.carsaleapi.domain.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.security.bcrypt-strength=4",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CarSaleIntegrationTest {

    private static final String PASSWORD = "Test-only-password-2026";

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private CommentRepository comments;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtEncoder jwtEncoder;
    @Autowired private jakarta.persistence.EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        saveUser("reader", "Leitor", "ROLE_USER");
        saveUser("admin", "Administrador", "ROLE_ADMIN");
    }

    @Test
    void blocksH2ConsoleOutsideDevelopment() throws Exception {
        mvc.perform(get("/h2-console/"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void listsCarsPubliclyWithoutLeakingCredentials() throws Exception {
        mvc.perform(get("/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].comments").isArray())
                .andExpect(content().string(not(org.hamcrest.Matchers.containsString("password"))));
    }

    @Test
    void readsCarAndCommentsPublicly() throws Exception {
        mvc.perform(get("/cars/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Corolla"));
        mvc.perform(get("/comments")).andExpect(status().isOk());
    }

    @Test
    void readsSingleCommentPublicly() throws Exception {
        long id = createComment(login("reader"), "Comentário público");
        mvc.perform(get("/comments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Comentário público"));
    }

    @Test
    void authenticatesWithEncodedPassword() throws Exception {
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"reader\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.name").value("Leitor"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andExpect(jsonPath("$.password").doesNotExist());
        String hash = users.findByUsername("reader").orElseThrow().getPassword();
        assertThat(hash).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, hash)).isTrue();
    }

    @Test
    void rejectsIncorrectPasswordAndUnknownUserUniformly() throws Exception {
        for (String username : new String[]{"reader", "missing"}) {
            mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"" + username + "\",\"password\":\"incorrect\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos."));
        }
    }

    @Test
    void rejectsAnonymousCreation() throws Exception {
        mvc.perform(post("/comments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Teste\",\"carId\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void readsCurrentUserOnlyWithValidAuthentication() throws Exception {
        mvc.perform(get("/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/me").header("Authorization", "Bearer " + login("reader")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Leitor"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rejectsUnknownFieldsAndOversizedUtf8Password() throws Exception {
        mvc.perform(post("/comments").header("Authorization", "Bearer " + login("reader"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Teste\",\"carId\":1,\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"reader\",\"password\":\"" + "á".repeat(40) + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAnonymousDeletion() throws Exception {
        mvc.perform(delete("/comments/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAuthenticatedUserAndAdminToComment() throws Exception {
        for (String username : new String[]{"reader", "admin"}) {
            long id = createComment(login(username), "Comentário válido");
            assertThat(comments.findById(id)).isPresent();
        }
    }

    @Test
    void derivesAuthorFromTokenIgnoringForgedUserId() throws Exception {
        var other = users.findByUsername("admin").orElseThrow();
        String body = mvc.perform(post("/comments")
                        .header("Authorization", "Bearer " + login("reader"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Autoria correta\",\"carId\":1,\"userId\":" + other.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Leitor"))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(body, "$.id")).longValue();
        assertThat(comments.findById(id).orElseThrow().getUser().getUsername()).isEqualTo("reader");
    }

    @Test
    void forbidsDeletionByRegularUserAndPreservesComment() throws Exception {
        String token = login("reader");
        long id = createComment(token, "Não pode apagar");
        mvc.perform(delete("/comments/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
        assertThat(comments.existsById(id)).isTrue();
    }

    @Test
    void allowsAdminToDeleteComment() throws Exception {
        long id = createComment(login("reader"), "Pode apagar");
        mvc.perform(delete("/comments/{id}", id).header("Authorization", "Bearer " + login("admin")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        assertThat(comments.existsById(id)).isFalse();
    }

    @Test
    void rejectsMissingCarAndMissingComment() throws Exception {
        mvc.perform(get("/cars/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/comments/999999")).andExpect(status().isNotFound());
        mvc.perform(post("/comments").header("Authorization", "Bearer " + login("reader"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Teste\",\"carId\":999999}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/comments/999999").header("Authorization", "Bearer " + login("admin")))
                .andExpect(status().isNotFound());
    }

    @Test
    void validatesEmptyOversizedAndMissingCommentFields() throws Exception {
        String token = login("reader");
        for (String body : new String[]{
                "{\"comment\":\"   \",\"carId\":1}",
                "{\"comment\":\"" + "x".repeat(2001) + "\",\"carId\":1}",
                "{\"comment\":\"Teste\",\"carId\":0}",
                "{\"comment\":\"Teste\"}",
                "{\"carId\":1}"}) {
            mvc.perform(post("/comments").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Test
    void validatesLoginBodyAndMalformedJson() throws Exception {
        for (String body : new String[]{"{}", "{\"username\":\" \",\"password\":\"x\"}", "{"}) {
            mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Test
    void rejectsInvalidPathIdentifier() throws Exception {
        mvc.perform(get("/cars/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/cars/0")).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsTamperedAndExpiredTokens() throws Exception {
        String token = login("reader");
        String[] parts = token.split("\\.");
        String forged = parts[0] + "." + parts[1] + "." + (parts[2].charAt(0) == 'A' ? "B" : "A") + parts[2].substring(1);
        for (String invalid : new String[]{forged, signedToken("carsale-api", Instant.now().minusSeconds(3600))}) {
            mvc.perform(post("/comments").header("Authorization", "Bearer " + invalid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"comment\":\"Teste\",\"carId\":1}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void rejectsTokenFromWrongIssuer() throws Exception {
        mvc.perform(post("/comments")
                        .header("Authorization", "Bearer " + signedToken("another-app", Instant.now().plusSeconds(300)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"comment\":\"Teste\",\"carId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deniesUnlistedWriteOperations() throws Exception {
        mvc.perform(post("/cars").header("Authorization", "Bearer " + login("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loadsCatalogWithoutNPlusOneQueries() throws Exception {
        var manager = entityManagerFactory.createEntityManager();
        manager.close();
        var stats = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.clear();
        mvc.perform(get("/cars")).andExpect(status().isOk());
        assertThat(stats.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }

    private void saveUser(String username, String name, String role) {
        var user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        users.saveAndFlush(user);
    }

    private String login(String username) throws Exception {
        String body = mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private long createComment(String token, String text) throws Exception {
        String body = mvc.perform(post("/comments").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"" + text + "\",\"carId\":1}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private String signedToken(String issuer, Instant expiresAt) {
        var claims = JwtClaimsSet.builder().issuer(issuer).subject("reader")
                .issuedAt(Instant.now().minusSeconds(7200)).expiresAt(expiresAt)
                .claim("role", "ROLE_USER").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
