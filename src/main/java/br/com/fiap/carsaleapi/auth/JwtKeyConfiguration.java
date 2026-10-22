package br.com.fiap.carsaleapi.auth;

import br.com.fiap.carsaleapi.props.RsaKeyProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class JwtKeyConfiguration {

    @Bean
    KeyPair jwtKeyPair(RsaKeyProperties properties, Environment environment) throws NoSuchAlgorithmException {
        if (properties.publicKey() != null && properties.privateKey() != null) {
            if (properties.publicKey().getModulus().bitLength() < 2048
                    || !properties.publicKey().getModulus().equals(properties.privateKey().getModulus())) {
                throw new IllegalStateException("Use um par RSA correspondente de no mínimo 2048 bits.");
            }
            return new KeyPair(properties.publicKey(), properties.privateKey());
        }
        if (environment.matchesProfiles("prod") || !environment.matchesProfiles("dev", "test")) {
            throw new IllegalStateException("Configure RSA_PUBLIC_KEY e RSA_PRIVATE_KEY com arquivos PEM externos.");
        }
        if (properties.publicKey() != null || properties.privateKey() != null) {
            throw new IllegalStateException("Configure as duas chaves RSA.");
        }
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair keys) {
        var decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keys.getPublic()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("carsale-api"));
        return decoder;
    }

    @Bean
    JwtEncoder jwtEncoder(KeyPair keys) {
        var jwk = new RSAKey.Builder((RSAPublicKey) keys.getPublic())
                .privateKey((RSAPrivateKey) keys.getPrivate()).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
