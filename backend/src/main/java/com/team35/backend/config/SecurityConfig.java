package com.team35.backend.config;

import com.team35.backend.exception.GlobalExceptionHandler;
import com.team35.backend.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jtwAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final GlobalExceptionHandler globalExceptionHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Desactivamos CSRF porque estamos usando JWT y no necesitamos protección contra CSRF en este caso
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/transacciones/clasificar-transaccion",
                                "/api/analisis/frecuencia-ahorro-encuesta",
                                "/api/analisis-financiero", "/api/clasificar-transaccion").permitAll()
                        .requestMatchers(HttpMethod.GET, "/","/health").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/","/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html" , "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                //agregar el filtro jtw antes del filtro de autenticación de usuario
                .addFilterBefore(jtwAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Agregar más orígenes permitidos
        configuration.setAllowedOrigins(
                List.of(
                        // Desarrollo local
                "http://localhost:5173",
                // Docker
                "http://frontend",
                "http://frontend:80",
                // Producción - Render
                        frontendUrl
                )
        );
        System.out.println(">>>FRONTEND URL: " + frontendUrl);
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        );
        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "Accept",
                        "Origin",
                        "Access-Control-Request-Method",
                        "Access-Control-Request-Headers"
                )
        );

        // Headers expuestos (para que el frontend pueda leerlos)
        configuration.setExposedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );
        // Permitir credenciales
        configuration.setAllowCredentials(true);
        // Tiempo de cache para preflight (3600 segundos = 1 hora)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    // Usar el GlobalExceptionHandler para errores de autenticación (401)
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            // El GlobalExceptionHandler manejará la excepción
            globalExceptionHandler.handleAuthenticationError(request, response, authException);
        };
    }


    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // El GlobalExceptionHandler manejará la excepción
            globalExceptionHandler.handleAccessDenied(request, response, accessDeniedException);
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
