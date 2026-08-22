package edu.demart_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * Route Access Matrix:
 * ┌──────────────────────────────┬────────────────────────────┐
 * │ Path Pattern                 │ Allowed Roles              │
 * ├──────────────────────────────┼────────────────────────────┤
 * │ POST /api/v1/auth/**         │ PUBLIC (no token needed)   │
 * │ GET  /api/v1/health          │ PUBLIC                     │
 * │ /api/v1/admin/**             │ ADMIN only                 │
 * │ /api/v1/staff/**             │ STAFF + ADMIN              │
 * │ Everything else              │ Any authenticated user     │
 * └──────────────────────────────┴────────────────────────────┘
 *
 * Fine-grained access control is applied via @PreAuthorize on individual
 * controller methods (enabled by @EnableMethodSecurity below).
 */
@Configuration
@EnableMethodSecurity          // enables @PreAuthorize / @PostAuthorize on methods
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — we are a stateless REST API using Bearer tokens
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless — no HttpSession, each request must carry its own JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Route-level access control
            .authorizeHttpRequests(auth -> auth

                // Public endpoints — no token required
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/health",
                    "/api/v1/categories",
                    "/api/v1/categories/**",
                    "/api/v1/products",
                    "/api/v1/products/**"
                ).permitAll()

                // Admin-only management routes
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // Staff routes (store operations) — accessible by STAFF and ADMIN
                .requestMatchers("/api/v1/staff/**").hasAnyRole("STAFF", "ADMIN")

                // Every other endpoint requires a valid authenticated token
                .anyRequest().authenticated()
            )

            // Register our JWT filter before Spring's default form-login filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}