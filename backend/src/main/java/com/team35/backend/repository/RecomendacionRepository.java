package com.team35.backend.repository;

import com.team35.backend.entity.Recomendacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {
    List<Recomendacion> findByAnalisisId(Long analisisId);
}
