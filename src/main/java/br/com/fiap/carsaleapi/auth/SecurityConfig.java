package br.com.fiap.carsaleapi.auth;

import br.com.fiap.carsaleapi.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/cars", "/cars/*", "/comments", "/comments/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/comments").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/comments/*").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(resource -> resource
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setHeader("WWW-Authenticate", "Bearer");
                            response.setContentType("application/json;charset=UTF-8");
                            mapper.writeValue(response.getOutputStream(), new ApiError(401, "Autenticação necessária ou token inválido."));
                        }))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setHeader("WWW-Authenticate", "Bearer");
                            response.setContentType("application/json;charset=UTF-8");
                            mapper.writeValue(response.getOutputStream(), new ApiError(401, "Autenticação necessária ou token inválido."));
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            mapper.writeValue(response.getOutputStream(), new ApiError(403, "Acesso não permitido."));
                        }))
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.disable())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder(@Value("${app.security.bcrypt-strength:12}") int strength) {
        return new BCryptPasswordEncoder(strength);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins:http://localhost:3000}") String origins) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::strip).toList());
        config.setAllowedMethods(java.util.List.of("GET", "POST", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(java.util.List.of("Location"));
        config.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthorityPrefix("");
        authorities.setAuthoritiesClaimName("role");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
