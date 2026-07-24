package com.team35.backend.service;

import com.team35.backend.dto.UsuarioDetails;
import com.team35.backend.dto.UsuarioRegister;
import com.team35.backend.entity.Usuario;
import com.team35.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Contiene la lógica relacionada con la autenticación y el registro de usuarios.

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* Registra un nuevo usuario:
     * 1. Verificamos que el email no esté registrado.
     * 2. Se encripta la contraseña mediante BCrypt.
     * 3. Se crea la entidad Usuario.
     * 4. Se guarda el usuario en la base de datos.
     * 5. Devolvemos únicamente información pública.
     */
    @Transactional
    public UsuarioDetails registrar(UsuarioRegister datos) {

        if (usuarioRepository.existsByEmail(datos.getEmail())) {
            throw new IllegalArgumentException(
                    "El correo electrónico ya está registrado"
            );
        }

        String passwordHash = passwordEncoder.encode(
                datos.getPassword()
        );

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre());
        usuario.setEmail(datos.getEmail());

        usuario.setPasswordHash(passwordHash);
        //valor controlado por el sistema, no por el usuario
        usuario.setActivo(true);

        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);
        //Devolvemos un DTO con información pública del usuario, sin incluir la contraseña ni otros datos sensibles.
        return new UsuarioDetails(
                usuarioGuardado.getId(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getEmail()
        );
    }
}
