package com.team35.backend.entity;

import com.team35.backend.enums.PerfilTipo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analisis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PerfilTipo perfil;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal probabilidad;

    @Column(name = "ingreso_mensual", nullable = false, precision = 12, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(name = "nivel_endeudamiento", nullable = false, precision = 5, scale = 2)
    private BigDecimal nivelEndeudamiento;

    @Column(name = "frecuencia_ahorro", nullable = false, length = 30)
    private String frecuenciaAhorro;

    @Column(name = "fecha_analisis", nullable = false)
    private LocalDateTime fechaAnalisis;

    @Builder.Default
    @OneToMany(mappedBy = "analisis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Recomendacion> recomendaciones = new ArrayList<>();

    @PrePersist
    protected void alCrear() {
        if (fechaAnalisis == null) {
            fechaAnalisis = LocalDateTime.now();
        }
    }

    public void agregarRecomendacion(Recomendacion recomendacion) {
        recomendacion.setAnalisis(this);
        this.recomendaciones.add(recomendacion);
    }
}
