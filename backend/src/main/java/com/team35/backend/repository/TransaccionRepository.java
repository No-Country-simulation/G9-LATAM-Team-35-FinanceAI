package com.team35.backend.repository;

import com.team35.backend.entity.Transaccion;
import com.team35.backend.enums.TipoTransaccion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByUsuarioId(Long usuarioId);

    // Útil para el flujo de análisis: traer solo las transacciones indicadas,
    // validando de paso que pertenezcan al usuario dueño de la sesión.
    List<Transaccion> findByIdInAndUsuarioId(List<Long> ids, Long usuarioId);

    // Transacciones que aún no han sido clasificadas (categoria_id es NULL)
    List<Transaccion> findByUsuarioIdAndCategoriaIsNull(Long usuarioId);

    /* Obtiene las transacciones de un usuario filtradas por su tipo: INGRESO,GASTO
     Ejemplo: findByUsuarioIdAndTipo(usuarioId, TipoTransaccion.GASTO)
    */
    List<Transaccion> findByUsuarioIdAndTipo(
            Long usuarioId,
            TipoTransaccion tipo
    );

    /* Obtiene las transacciones de un usuario dentro de un período de fechas determinado.
     Puede utilizarse para obtener las transacciones de un mes específico.
     */
    List<Transaccion> findByUsuarioIdAndFechaBetween(
            Long usuarioId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    Optional<Transaccion> findByIdAndUsuarioId(Long id, Long usuarioId);

    // Calcula la suma de los ingresos de un usuario dentro de un período determinado.
    @Query("""
        SELECT COALESCE(SUM(t.valor), 0)
        FROM Transaccion t
        WHERE t.usuario.id = :usuarioId
          AND t.tipo = :tipo
          AND t.fecha BETWEEN :fechaInicio AND :fechaFin
    """)
    BigDecimal calcularTotalPorTipoYPeriodo(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoTransaccion tipo,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    List<Transaccion> findByUsuarioIdAndDescripcionContainingIgnoreCase(Long usuarioId, String descripcion);

}
