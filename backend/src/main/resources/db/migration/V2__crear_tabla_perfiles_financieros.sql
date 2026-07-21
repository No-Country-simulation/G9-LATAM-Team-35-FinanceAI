CREATE TABLE perfiles_financieros (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    ingreso_mensual DECIMAL(12, 2) NOT NULL,
    nivel_endeudamiento DECIMAL(5, 2) NOT NULL,
    frecuencia_ahorro VARCHAR(30) NOT NULL,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_perfil_financiero_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
);