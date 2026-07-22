package com.team35.backend.repository;

import com.team35.backend.entity.PerfilFinanciero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilFinancieroRepository extends JpaRepository<PerfilFinanciero, Long> {
    Optional<PerfilFinanciero> findByUsuarioId(Long usuarioId);
}
