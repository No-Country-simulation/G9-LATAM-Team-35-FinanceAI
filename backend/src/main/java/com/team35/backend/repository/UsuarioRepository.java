package com.team35.backend.repository;

import com.team35.backend.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(@NotBlank(message = "El correo electrónico es obligatorio") @Email(message = "El correo electrónico no tiene un formato válido") String email);
}
