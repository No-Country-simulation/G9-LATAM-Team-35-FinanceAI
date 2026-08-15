package com.team35.backend.service;

import com.team35.backend.dto.ActualizarUsuarioRequest;
import com.team35.backend.dto.MonedaDisponibleDTO;
import com.team35.backend.dto.UsuarioPerfilResponse;
import com.team35.backend.entity.Usuario;
import com.team35.backend.enums.Moneda;
import com.team35.backend.repository.UsuarioRepository;
import com.team35.backend.util.MonedaTextoMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioPerfilResponse obtenerPerfil(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        return aPerfilResponse(usuario);
    }

    public UsuarioPerfilResponse actualizarPerfil(Long usuarioId, ActualizarUsuarioRequest request) {
        Usuario usuario = buscarUsuario(usuarioId);

        Moneda moneda = parsearMoneda(request.getMoneda());

        usuario.setNombre(request.getNombre());
        usuario.setMoneda(moneda);

        Usuario actualizado = usuarioRepository.save(usuario);
        return aPerfilResponse(actualizado);
    }

    public List<MonedaDisponibleDTO> listarMonedasDisponibles() {
        return Arrays.stream(Moneda.values())
                .map(m -> new MonedaDisponibleDTO(m.name(), MonedaTextoMapper.aTexto(m)))
                .toList();
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));
    }

    private Moneda parsearMoneda(String valor) {
        try {
            return Moneda.valueOf(valor.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new MonedaInvalidaException(valor);
        }
    }

    private UsuarioPerfilResponse aPerfilResponse(Usuario usuario) {
        return new UsuarioPerfilResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getMoneda().name(),
                MonedaTextoMapper.aTexto(usuario.getMoneda())
        );
    }

    // Excepciones simples, específicas de este service; el GlobalExceptionHandler
    // ya tiene el catch-all genérico, pero estas dan un mensaje más claro al frontend.
    public static class UsuarioNoEncontradoException extends RuntimeException {
        public UsuarioNoEncontradoException(Long usuarioId) {
            super("No existe un usuario con id " + usuarioId);
        }
    }

    public static class MonedaInvalidaException extends RuntimeException {
        public MonedaInvalidaException(String valor) {
            super("'" + valor + "' no es una moneda válida. Usa GET /usuarios/monedas-disponibles para ver las opciones.");
        }
    }
}
