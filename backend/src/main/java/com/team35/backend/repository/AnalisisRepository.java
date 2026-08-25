package com.team35.backend.repository;

import com.team35.backend.entity.Analisis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnalisisRepository extends JpaRepository<Analisis, Long> {

    // Historial de análisis del usuario, más reciente primero
    List<Analisis> findByUsuarioIdOrderByFechaAnalisisDesc(Long usuarioId);

    @Query(value = """
    SELECT * FROM analisis 
    WHERE usuario_id = :usuarioId 
    AND CONCAT(
        TO_CHAR(fecha_analisis, 'TMMonth YYYY'), 
        ' - ', 
        perfil
    ) ILIKE CONCAT('%', :query, '%')
    ORDER BY fecha_analisis DESC
    """, nativeQuery = true)

    List<Analisis> buscarPorNombreGenerado(@Param("usuarioId") Long usuarioId, @Param("query") String query);

    // Obtener análisis con sus recomendaciones
    @Query("SELECT a FROM Analisis a " +
            "LEFT JOIN FETCH a.recomendaciones " +
            "WHERE a.usuario.id = :usuarioId " +
            "ORDER BY a.fechaAnalisis DESC")
    List<Analisis> findByUsuarioIdWithRecomendaciones(Long usuarioId);

    // Obtener un análisis específico con sus recomendaciones
    @Query("SELECT a FROM Analisis a " +
            "LEFT JOIN FETCH a.recomendaciones " +
            "WHERE a.id = :id AND a.usuario.id = :usuarioId")
    Optional<Analisis> findByIdAndUsuarioIdWithRecomendaciones(Long id, Long usuarioId);

    // Obtener el análisis más reciente con recomendaciones
    @Query("SELECT a FROM Analisis a " +
            "LEFT JOIN FETCH a.recomendaciones " +
            "WHERE a.usuario.id = :usuarioId " +
            "ORDER BY a.fechaAnalisis DESC")
    List<Analisis> findTopByUsuarioIdOrderByFechaAnalisisDesc(Long usuarioId);
}
