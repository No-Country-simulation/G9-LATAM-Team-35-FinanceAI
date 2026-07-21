CREATE TABLE recomendaciones (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    analisis_id BIGINT NOT NULL,
    texto TEXT NOT NULL,

    CONSTRAINT fk_recomendacion_analisis
        FOREIGN KEY (analisis_id)
        REFERENCES analisis(id)
        ON DELETE CASCADE
);