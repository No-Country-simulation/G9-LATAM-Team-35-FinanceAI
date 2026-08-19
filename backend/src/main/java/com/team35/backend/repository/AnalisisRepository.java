package com.team35.backend.repository;

import com.team35.backend.entity.Analisis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalisisRepository extends JpaRepository<Analisis, Long> {

    // Historial de análisis del usuario, más reciente primero
    List<Analisis> findByUsuarioIdOrderByFechaAnalisisDesc(Long usuarioId);
}
