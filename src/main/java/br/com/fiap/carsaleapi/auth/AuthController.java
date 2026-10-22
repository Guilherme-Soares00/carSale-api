package br.com.fiap.carsaleapi.auth;

import br.com.fiap.carsaleapi.domain.user.UserRepository;
import br.com.fiap.carsaleapi.domain.user.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository users;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService, UserRepository users) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.users = users;
    }

    @PostMapping("/login")
    public Token login(@RequestBody @Valid LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("Credenciais inválidas");
        }
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        return tokenService.generateToken(authentication);
    }

    @GetMapping("/me")
    public UserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        var user = users.findByUsername(jwt.getSubject())
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));
        return new UserResponse(user.getId(), user.getName(), user.getRole());
    }
}
