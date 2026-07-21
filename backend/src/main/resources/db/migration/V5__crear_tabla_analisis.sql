CREATE TABLE analisis (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    perfil VARCHAR(30) NOT NULL,
    probabilidad DECIMAL(5, 4) NOT NULL,
    ingreso_mensual DECIMAL(12, 2) NOT NULL,
    nivel_endeudamiento DECIMAL(5, 2) NOT NULL,
    frecuencia_ahorro VARCHAR(30) NOT NULL,
    fecha_analisis TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_analisis_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id),

    CONSTRAINT chk_analisis_perfil
        CHECK (perfil IN ('SALUDABLE', 'EN_OBSERVACION', 'EN_RIESGO')),

    CONSTRAINT chk_analisis_probabilidad
        CHECK (probabilidad >= 0 AND probabilidad <= 1)
);