-- Desactivar categorías que no utiliza el clasificador
UPDATE categorias
SET activo = false
WHERE nombre IN ('OCIO', 'SERVICIOS');

-- Agregar la nueva categoría combinada
INSERT INTO categorias (nombre, descripcion, activo)
VALUES (
    'OCIO Y SERVICIOS',
    'Entretenimiento, streaming, salidas, luz, agua, internet y teléfono',
    true
);