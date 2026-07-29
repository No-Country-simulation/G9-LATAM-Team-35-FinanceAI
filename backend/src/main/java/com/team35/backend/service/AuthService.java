package com.team35.backend.service;

import com.team35.backend.dto.LoginDetails;
import com.team35.backend.dto.UsuarioDetails;
import com.team35.backend.dto.UsuarioLogin;
import com.team35.backend.dto.UsuarioRegister;
import com.team35.backend.entity.Usuario;
import com.team35.backend.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Contiene la lógica relacionada con la autenticación y el registro de usuarios.

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
    /*
     * Metodo para autenticar a un usuario:
     * 1. Se busca el usuario por email.
     * 2. Se compara la contraseña proporcionada con la almacenada (hash).
     * 3. Si es correcta, se devuelve un DTO con información pública del usuario.
     */
    @Transactional
    public LoginDetails login (UsuarioLogin datos) {
        //Buscamos el usuario por email
        Usuario usuario = usuarioRepository.findByEmail(datos.getEmail())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuario o contraseña incorrectos"
                        )
                );
        //verificar que la cuenta esté activa
        if (!usuario.getActivo()) {
            throw new IllegalArgumentException(
                    "La cuenta de usuario está desactivada"
            );
        }
        //comparamos la contraseña proporcionada con la almacenada (hash)
        boolean passwordCorrecta = passwordEncoder.matches(
                datos.getPassword(),
                usuario.getPasswordHash()
        );
        if (!passwordCorrecta) {
            throw new IllegalArgumentException(
                    "Usuario o contraseña incorrectos"
            );
        }

        //generamos el jwt para el usuario autenticado
        String token = jwtService.generarToken(usuario.getEmail());
        //actualizamos la fecha de último acceso del usuario
        usuario.setUltimoAcceso(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        //Devolvemos un DTO con información pública del usuario y el token JWT.
        return new LoginDetails(
                token,
                new UsuarioDetails(
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getEmail()
                )
        );
    }

    //métodos para obtener el usuario autenticado
    public Usuario getUsuarioAutenticado(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
    }
    //obtiene el email del usuario autenticado
    public String getEmailUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return authentication.getName();
    }

    //verifica si hay un usuario autenticado
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    //obtiene el id del usuario autenticado
    public Long getUsuarioIdAutenticado() {
        return getUsuarioAutenticado().getId();
    }
}
