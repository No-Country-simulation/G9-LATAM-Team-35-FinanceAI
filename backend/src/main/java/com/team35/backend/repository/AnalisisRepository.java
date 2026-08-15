package com.team35.backend.repository;

import com.team35.backend.entity.Analisis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}
