package com.team35.backend.repository;

import com.team35.backend.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByUsuarioId(Long usuarioId);

    // Útil para el flujo de análisis: traer solo las transacciones indicadas,
    // validando de paso que pertenezcan al usuario dueño de la sesión.
    List<Transaccion> findByIdInAndUsuarioId(List<Long> ids, Long usuarioId);

    // Transacciones que aún no han sido clasificadas (categoria_id es NULL)
    List<Transaccion> findByUsuarioIdAndCategoriaIsNull(Long usuarioId);
}
