package com.team35.backend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // Desactivamos CSRF temporalmente para probar las peticiones POST desde Postman sin enviar un token CSRF.
                .csrf(csrf -> csrf.disable())
                /* Temporalmente permitimos todas las solicitudes.
                 * Esto es únicamente para realizar pruebas mientras todavía no implementamos la autenticación definitiva con JWT.
                 */
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
