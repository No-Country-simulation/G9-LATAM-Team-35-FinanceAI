package com.team35.backend.repository;

import com.team35.backend.entity.Transaccion;
import com.team35.backend.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByUsuarioId(Long usuarioId);

    // Útil para el flujo de análisis: traer solo las transacciones indicadas,
    // validando de paso que pertenezcan al usuario dueño de la sesión.
    List<Transaccion> findByIdInAndUsuarioId(List<Long> ids, Long usuarioId);

    // Transacciones que aún no han sido clasificadas (categoria_id es NULL)
    List<Transaccion> findByUsuarioIdAndCategoriaIsNull(Long usuarioId);

    // Para "distribución de gastos": solo GASTO ya clasificados (categoria no nula),
    // dentro de un rango de fechas (normalmente un mes). NO corre clasificación de IA,
    // solo lee lo que ya está guardado en BD.
    List<Transaccion> findByUsuarioIdAndTipoAndCategoriaIsNotNullAndFechaBetween(
            Long usuarioId, TipoTransaccion tipo, LocalDate desde, LocalDate hasta);
}
