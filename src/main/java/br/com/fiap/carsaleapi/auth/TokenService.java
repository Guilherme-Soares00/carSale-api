package br.com.fiap.carsaleapi.auth;

import br.com.fiap.carsaleapi.domain.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class TokenService {
    private static final long TOKEN_SECONDS = 1800;
    private final JwtEncoder encoder;
    private final Clock clock;

    public TokenService(JwtEncoder encoder, Clock clock) {
        this.encoder = encoder;
        this.clock = clock;
    }

    public Token generateToken(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        var now = clock.instant();
        var claims = JwtClaimsSet.builder().issuer("carsale-api")
                .issuedAt(now).expiresAt(now.plusSeconds(TOKEN_SECONDS))
                .subject(user.getUsername()).claim("role", user.getRole()).build();
        var token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new Token(token, user.getName(), user.getId().toString(), user.getRole(), TOKEN_SECONDS);
    }
}
