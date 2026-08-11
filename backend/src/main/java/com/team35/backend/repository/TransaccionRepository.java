package com.team35.backend.repository;

import com.team35.backend.entity.Transaccion;
import com.team35.backend.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    // Transacciones que aún no han sido clasificadas (categoria_id es NULL)
    List<Transaccion> findByUsuarioIdAndCategoriaIsNull(Long usuarioId);

    /*
     * Obtiene las transacciones de un usuario filtradas por su tipo:
     * INGRESO, GASTO.
     */
    List<Transaccion> findByUsuarioIdAndTipo(
            Long usuarioId,
            TipoTransaccion tipo
    );

    /*
     * Obtiene las transacciones de un usuario dentro de un período
     * de fechas determinado.
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

    List<Transaccion> findByUsuarioIdAndDescripcionContainingIgnoreCase(
            Long usuarioId,
            String descripcion
    );

    // Para "distribución de gastos": solo GASTO ya clasificados
    // (categoria no nula), dentro de un rango de fechas.
    List<Transaccion> findByUsuarioIdAndTipoAndCategoriaIsNotNullAndFechaBetween(
            Long usuarioId,
            TipoTransaccion tipo,
            LocalDate desde,
            LocalDate hasta
    );
}
