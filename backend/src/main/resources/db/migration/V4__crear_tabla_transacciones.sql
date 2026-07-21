CREATE TABLE transacciones (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    valor DECIMAL(12, 2) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    categoria_id BIGINT,
    fecha DATE NOT NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaccion_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id),

    CONSTRAINT fk_transaccion_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categorias(id),

    CONSTRAINT chk_transaccion_tipo
        CHECK (tipo IN ('INGRESO', 'GASTO')),

    CONSTRAINT chk_transaccion_valor
        CHECK (valor > 0)
);