-- PROPUESTA: aún no está en el repo compartido, primero coordinar con quien maneja
-- las migraciones para que la agregue como V7 (o el número que corresponda).
--
-- Sin esto, la tabla "categorias" queda vacía y no se puede asignar categoria_id
-- a ninguna transacción (violaría la foreign key fk_transaccion_categoria).

INSERT INTO categorias (nombre, descripcion, activo) VALUES
    ('ALIMENTACION', 'Gastos relacionados con comida y supermercado', true),
    ('TRANSPORTE', 'Combustible, transporte público, viajes', true),
    ('SALUD', 'Farmacia, consultas médicas, seguros de salud', true),
    ('VIVIENDA', 'Alquiler, hipoteca, mantenimiento del hogar', true),
    ('EDUCACION', 'Colegiaturas, cursos, materiales de estudio', true),
    ('OCIO', 'Entretenimiento, streaming, salidas', true),
    ('SERVICIOS', 'Luz, agua, internet, teléfono', true),
    ('OTROS', 'Gastos que no encajan en otra categoría', true);
