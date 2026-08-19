-- ============================================================
-- V10 · Seed de usuario demo para Finance AI
-- ------------------------------------------------------------
-- Credenciales de acceso:
--   Email    : demo@financeai.app
--   Password : Demo1234!
-- ------------------------------------------------------------
-- Perfil: Carlos Demo · ingreso $18,000 MXN/mes
-- Período: Marzo 2026 → Agosto 2026 (6 meses)
-- Tendencia: de SALUDABLE → EN_OBSERVACION (deuda subió del 45% al 60%)
-- ============================================================

-- Guard: no duplicar si ya existe
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM usuarios WHERE email = 'demo@financeai.app') THEN
    RAISE NOTICE 'Usuario demo ya existe. Se omite el seed V10.';
    RETURN;
  END IF;
END $$;

-- ============================================================
-- 1. USUARIO DEMO
-- ============================================================
INSERT INTO usuarios (email, password_hash, nombre, creado_en, activo, moneda)
VALUES (
    'demo@financeai.app',
    '$2b$10$IQu.VGPK4ezkicCZmqO.RexzJ5aKk6w1D5FluKqsXg5ZeL95KSezC',
    'Carlos Demo',
    '2026-02-28 10:00:00',
    true,
    'MXN'
);

-- ============================================================
-- 2. PERFIL FINANCIERO
-- ============================================================
INSERT INTO perfiles_financieros (usuario_id, ingreso_mensual, nivel_endeudamiento, frecuencia_ahorro, actualizado_en)
SELECT id, 18000.00, 60.00, 'BAJA', '2026-08-10 09:00:00'
FROM   usuarios
WHERE  email = 'demo@financeai.app';

-- ============================================================
-- 3. TRANSACCIONES (6 meses × ~22 por mes = ~132 registros)
--    Distribución mensual de gastos:
--    • VIVIENDA        ~25%  ($3,750)
--    • ALIMENTACION    ~35%  ($5,250)
--    • TRANSPORTE      ~20%  ($3,000)
--    • SALUD           ~5%   ($750)
--    • EDUCACION       ~5%   ($750)
--    • OCIO Y SERVICIOS ~10% ($1,500)
--    Total gasto:  ~$15,000  |  Ingreso: $18,000
-- ============================================================

-- Helper local para no repetir el subquery
DO $$
DECLARE
    v_uid       BIGINT;
    v_cat_ali   BIGINT;
    v_cat_tra   BIGINT;
    v_cat_sal   BIGINT;
    v_cat_viv   BIGINT;
    v_cat_edu   BIGINT;
    v_cat_oci   BIGINT;
    v_cat_otr   BIGINT;
BEGIN
    SELECT id INTO v_uid FROM usuarios WHERE email = 'demo@financeai.app';

    SELECT id INTO v_cat_ali FROM categorias WHERE nombre = 'ALIMENTACION' AND activo = true LIMIT 1;
    SELECT id INTO v_cat_tra FROM categorias WHERE nombre = 'TRANSPORTE'   AND activo = true LIMIT 1;
    SELECT id INTO v_cat_sal FROM categorias WHERE nombre = 'SALUD'        AND activo = true LIMIT 1;
    SELECT id INTO v_cat_viv FROM categorias WHERE nombre = 'VIVIENDA'     AND activo = true LIMIT 1;
    SELECT id INTO v_cat_edu FROM categorias WHERE nombre = 'EDUCACION'    AND activo = true LIMIT 1;
    SELECT id INTO v_cat_oci FROM categorias WHERE nombre = 'OCIO Y SERVICIOS' AND activo = true LIMIT 1;
    SELECT id INTO v_cat_otr FROM categorias WHERE nombre = 'OTROS'        AND activo = true LIMIT 1;

    -- --------------------------------------------------------
    -- MARZO 2026 — SALUDABLE (gasto ~14,800, ahorro ~3,200)
    -- --------------------------------------------------------
    -- Ingresos
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Nómina marzo', 18000.00, 'INGRESO', NULL, '2026-03-01');

    -- Vivienda
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Renta departamento', 3750.00, 'GASTO', v_cat_viv, '2026-03-01');

    -- Alimentación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'COMPRA EN WALMART',            820.50, 'GASTO', v_cat_ali, '2026-03-03'),
    (v_uid, 'pago con tarjeta en Soriana',  670.30, 'GASTO', v_cat_ali, '2026-03-10'),
    (v_uid, 'Uber Eats',                    348.00, 'GASTO', v_cat_ali, '2026-03-12'),
    (v_uid, 'pago restaurante',             412.00, 'GASTO', v_cat_ali, '2026-03-15'),
    (v_uid, 'OXXO',                          95.50, 'GASTO', v_cat_ali, '2026-03-18'),
    (v_uid, 'Rappi',                        265.00, 'GASTO', v_cat_ali, '2026-03-22'),
    (v_uid, '$Starbucks',                   198.00, 'GASTO', v_cat_ali, '2026-03-25'),
    (v_uid, 'COMPRA EN SUPERMERCADO',       740.00, 'GASTO', v_cat_ali, '2026-03-28');

    -- Transporte
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'carga gasolina PEMEX',    750.00, 'GASTO', v_cat_tra, '2026-03-05'),
    (v_uid, 'pago Uber',               185.00, 'GASTO', v_cat_tra, '2026-03-11'),
    (v_uid, 'carga gasolina PEMEX',    720.00, 'GASTO', v_cat_tra, '2026-03-20'),
    (v_uid, 'Didi',                    210.00, 'GASTO', v_cat_tra, '2026-03-26');

    -- Salud
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Farmacia Benavides', 620.00, 'GASTO', v_cat_sal, '2026-03-08');

    -- Educación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Curso Udemy Python', 450.00, 'GASTO', v_cat_edu, '2026-03-14');

    -- Ocio y Servicios
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'CFE energia electrica',  580.00, 'GASTO', v_cat_oci, '2026-03-06'),
    (v_uid, 'TELMEX internet',        399.00, 'GASTO', v_cat_oci, '2026-03-07'),
    (v_uid, 'Netflix suscripcion',    219.00, 'GASTO', v_cat_oci, '2026-03-16');

    -- --------------------------------------------------------
    -- ABRIL 2026 — SALUDABLE (gasto ~14,980, ahorro ~3,020)
    -- --------------------------------------------------------
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Nómina abril', 18000.00, 'INGRESO', NULL, '2026-04-01');

    -- Vivienda
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Renta departamento', 3750.00, 'GASTO', v_cat_viv, '2026-04-01');

    -- Alimentación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'COSTCO',                       925.00, 'GASTO', v_cat_ali, '2026-04-02'),
    (v_uid, 'pago con tarjeta en Chedraui', 580.00, 'GASTO', v_cat_ali, '2026-04-09'),
    (v_uid, 'Didi Food',                    398.00, 'GASTO', v_cat_ali, '2026-04-13'),
    (v_uid, 'COMPRA EN RESTAURANTE',        475.00, 'GASTO', v_cat_ali, '2026-04-17'),
    (v_uid, '$7-Eleven',                    112.00, 'GASTO', v_cat_ali, '2026-04-19'),
    (v_uid, 'Rappi',                        310.00, 'GASTO', v_cat_ali, '2026-04-22'),
    (v_uid, 'pago McDonalds',               275.00, 'GASTO', v_cat_ali, '2026-04-24'),
    (v_uid, '$Bodega Aurrera',              695.00, 'GASTO', v_cat_ali, '2026-04-27');

    -- Transporte
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'carga gasolina PEMEX',    780.00, 'GASTO', v_cat_tra, '2026-04-04'),
    (v_uid, 'pago Uber',               230.00, 'GASTO', v_cat_tra, '2026-04-12'),
    (v_uid, 'carga gasolina PEMEX',    760.00, 'GASTO', v_cat_tra, '2026-04-21'),
    (v_uid, 'Didi traslado',           195.00, 'GASTO', v_cat_tra, '2026-04-28');

    -- Salud
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'consulta medico general', 500.00, 'GASTO', v_cat_sal, '2026-04-10');

    -- Educación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'libros tecnica', 380.00, 'GASTO', v_cat_edu, '2026-04-16');

    -- Ocio y Servicios
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'CFE energia electrica',  495.00, 'GASTO', v_cat_oci, '2026-04-05'),
    (v_uid, 'TELMEX internet',        399.00, 'GASTO', v_cat_oci, '2026-04-07'),
    (v_uid, 'Spotify premium',        119.00, 'GASTO', v_cat_oci, '2026-04-15');

    -- --------------------------------------------------------
    -- MAYO 2026 — EN_OBSERVACION (gasto ~16,100, ahorro ~1,900)
    -- --------------------------------------------------------
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Nómina mayo', 18000.00, 'INGRESO', NULL, '2026-05-01');

    -- Vivienda
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Renta departamento', 3750.00, 'GASTO', v_cat_viv, '2026-05-01');

    -- Alimentación — mes con gasto extra (salidas)
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'COMPRA EN WALMART',             1050.00, 'GASTO', v_cat_ali, '2026-05-03'),
    (v_uid, 'pago con tarjeta en Soriana',    760.00, 'GASTO', v_cat_ali, '2026-05-08'),
    (v_uid, '*Uber Eats*',                    542.00, 'GASTO', v_cat_ali, '2026-05-11'),
    (v_uid, 'pago con tarjeta en restaurante',620.00, 'GASTO', v_cat_ali, '2026-05-14'),
    (v_uid, 'compra OXXO',                    145.00, 'GASTO', v_cat_ali, '2026-05-17'),
    (v_uid, 'Didi Food',                      480.00, 'GASTO', v_cat_ali, '2026-05-20'),
    (v_uid, '$Starbucks',                     320.00, 'GASTO', v_cat_ali, '2026-05-23'),
    (v_uid, 'Restaurante',                    510.00, 'GASTO', v_cat_ali, '2026-05-28');

    -- Transporte
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'carga gasolina Shell',    820.00, 'GASTO', v_cat_tra, '2026-05-06'),
    (v_uid, 'pago Uber',               280.00, 'GASTO', v_cat_tra, '2026-05-13'),
    (v_uid, 'carga gasolina PEMEX',    800.00, 'GASTO', v_cat_tra, '2026-05-22'),
    (v_uid, 'Didi express',            350.00, 'GASTO', v_cat_tra, '2026-05-29');

    -- Salud
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Farmacia del Ahorro', 740.00, 'GASTO', v_cat_sal, '2026-05-09');

    -- Educación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Certificacion AWS', 680.00, 'GASTO', v_cat_edu, '2026-05-15');

    -- Ocio y Servicios
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'CFE energia electrica',  720.00, 'GASTO', v_cat_oci, '2026-05-05'),
    (v_uid, 'TELMEX internet',        399.00, 'GASTO', v_cat_oci, '2026-05-07'),
    (v_uid, 'Netflix + Disney+',      348.00, 'GASTO', v_cat_oci, '2026-05-16');

    -- --------------------------------------------------------
    -- JUNIO 2026 — EN_OBSERVACION (gasto ~16,300, ahorro ~1,700)
    -- --------------------------------------------------------
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Nómina junio', 18000.00, 'INGRESO', NULL, '2026-06-01');

    -- Vivienda
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Renta departamento', 3750.00, 'GASTO', v_cat_viv, '2026-06-01');

    -- Alimentación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'SAMS CLUB',                      980.00, 'GASTO', v_cat_ali, '2026-06-02'),
    (v_uid, '$Chedraui',                       650.00, 'GASTO', v_cat_ali, '2026-06-07'),
    (v_uid, 'Rappi',                           520.00, 'GASTO', v_cat_ali, '2026-06-11'),
    (v_uid, 'pago restaurante',                580.00, 'GASTO', v_cat_ali, '2026-06-14'),
    (v_uid, 'compra OXXO',                     130.00, 'GASTO', v_cat_ali, '2026-06-18'),
    (v_uid, 'COMPRA EN UBER EATS',             605.00, 'GASTO', v_cat_ali, '2026-06-21'),
    (v_uid, 'Burger King',                     340.00, 'GASTO', v_cat_ali, '2026-06-24'),
    (v_uid, '*Bodega Aurrera*',                785.00, 'GASTO', v_cat_ali, '2026-06-27');

    -- Transporte
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'carga gasolina PEMEX',    850.00, 'GASTO', v_cat_tra, '2026-06-04'),
    (v_uid, 'pago Uber',               310.00, 'GASTO', v_cat_tra, '2026-06-10'),
    (v_uid, 'carga gasolina Shell',    820.00, 'GASTO', v_cat_tra, '2026-06-20'),
    (v_uid, 'Didi traslado',           245.00, 'GASTO', v_cat_tra, '2026-06-26');

    -- Salud
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'consulta especialista', 800.00, 'GASTO', v_cat_sal, '2026-06-12');

    -- Educación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Coursera suscripcion', 420.00, 'GASTO', v_cat_edu, '2026-06-17');

    -- Ocio y Servicios
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'CFE energia electrica',  655.00, 'GASTO', v_cat_oci, '2026-06-05'),
    (v_uid, 'TELMEX internet',        399.00, 'GASTO', v_cat_oci, '2026-06-07'),
    (v_uid, 'Netflix suscripcion',    219.00, 'GASTO', v_cat_oci, '2026-06-16');

    -- --------------------------------------------------------
    -- JULIO 2026 — EN_OBSERVACION (gasto ~16,700, ahorro ~1,300)
    -- --------------------------------------------------------
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Nómina julio', 18000.00, 'INGRESO', NULL, '2026-07-01');

    -- Vivienda
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Renta departamento', 3750.00, 'GASTO', v_cat_viv, '2026-07-01');

    -- Alimentación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'COMPRA EN WALMART',              1120.00, 'GASTO', v_cat_ali, '2026-07-03'),
    (v_uid, 'pago Costco',                     890.00, 'GASTO', v_cat_ali, '2026-07-08'),
    (v_uid, '*Uber Eats*',                     610.00, 'GASTO', v_cat_ali, '2026-07-11'),
    (v_uid, 'COMPRA EN RESTAURANTE',           685.00, 'GASTO', v_cat_ali, '2026-07-15'),
    (v_uid, 'pago OXXO',                       158.00, 'GASTO', v_cat_ali, '2026-07-18'),
    (v_uid, 'compra Rappi',                    542.00, 'GASTO', v_cat_ali, '2026-07-21'),
    (v_uid, 'DOMINOS',                         475.00, 'GASTO', v_cat_ali, '2026-07-24'),
    (v_uid, 'COMPRA EN SORIANA',               720.00, 'GASTO', v_cat_ali, '2026-07-28');

    -- Transporte
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'carga gasolina Shell',    880.00, 'GASTO', v_cat_tra, '2026-07-05'),
    (v_uid, 'pago Uber',               350.00, 'GASTO', v_cat_tra, '2026-07-13'),
    (v_uid, 'carga gasolina PEMEX',    840.00, 'GASTO', v_cat_tra, '2026-07-22'),
    (v_uid, 'Didi express',            290.00, 'GASTO', v_cat_tra, '2026-07-29');

    -- Salud
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Farmacia Benavides', 480.00, 'GASTO', v_cat_sal, '2026-07-09');

    -- Educación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Udemy paquete cursos', 560.00, 'GASTO', v_cat_edu, '2026-07-16');

    -- Ocio y Servicios
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'CFE energia electrica',  790.00, 'GASTO', v_cat_oci, '2026-07-06'),
    (v_uid, 'TELMEX internet',        399.00, 'GASTO', v_cat_oci, '2026-07-07'),
    (v_uid, 'Netflix + Spotify',      338.00, 'GASTO', v_cat_oci, '2026-07-16');

    -- --------------------------------------------------------
    -- AGOSTO 2026 — EN_OBSERVACION crítico (gasto ~16,900, ahorro ~1,100)
    -- Mes actual: alertas de liquidez y endeudamiento activadas
    -- --------------------------------------------------------
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Nómina agosto', 18000.00, 'INGRESO', NULL, '2026-08-01');

    -- Vivienda
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Renta departamento', 3750.00, 'GASTO', v_cat_viv, '2026-08-01');

    -- Alimentación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'COMPRA EN WALMART',              1200.00, 'GASTO', v_cat_ali, '2026-08-02'),
    (v_uid, 'pago con tarjeta en Chedraui',   820.00, 'GASTO', v_cat_ali, '2026-08-06'),
    (v_uid, 'Uber Eats',                       680.00, 'GASTO', v_cat_ali, '2026-08-09'),
    (v_uid, 'COMPRA EN RESTAURANTE',           750.00, 'GASTO', v_cat_ali, '2026-08-12'),
    (v_uid, 'compra OXXO',                     175.00, 'GASTO', v_cat_ali, '2026-08-14'),
    (v_uid, '*Rappi*',                         595.00, 'GASTO', v_cat_ali, '2026-08-16'),
    (v_uid, 'pago McDonalds',                  420.00, 'GASTO', v_cat_ali, '2026-08-18'),
    (v_uid, '*Bodega Aurrera*',                810.00, 'GASTO', v_cat_ali, '2026-08-19');

    -- Transporte
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'carga gasolina PEMEX',    900.00, 'GASTO', v_cat_tra, '2026-08-04'),
    (v_uid, 'pago Uber',               380.00, 'GASTO', v_cat_tra, '2026-08-10'),
    (v_uid, 'carga gasolina Shell',    870.00, 'GASTO', v_cat_tra, '2026-08-17');

    -- Salud
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'consulta medico + farmacia', 950.00, 'GASTO', v_cat_sal, '2026-08-08');

    -- Educación
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES (v_uid, 'Platzi suscripcion anual', 720.00, 'GASTO', v_cat_edu, '2026-08-13');

    -- Ocio y Servicios
    INSERT INTO transacciones (usuario_id, descripcion, valor, tipo, categoria_id, fecha)
    VALUES
    (v_uid, 'CFE energia electrica',  850.00, 'GASTO', v_cat_oci, '2026-08-05'),
    (v_uid, 'TELMEX internet',        399.00, 'GASTO', v_cat_oci, '2026-08-07'),
    (v_uid, 'Netflix suscripcion',    219.00, 'GASTO', v_cat_oci, '2026-08-15');

    -- ============================================================
    -- 4. ANALISIS PRE-CARGADOS (uno por mes)
    -- ============================================================

    -- Marzo: SALUDABLE
    WITH a AS (
        INSERT INTO analisis (usuario_id, perfil, probabilidad, ingreso_mensual, nivel_endeudamiento, frecuencia_ahorro, fecha_analisis)
        VALUES (v_uid, 'SALUDABLE', 0.7200, 18000.00, 45.00, 'BAJA', '2026-03-31 20:00:00')
        RETURNING id
    )
    INSERT INTO recomendaciones (analisis_id, texto)
    SELECT a.id, r.txt FROM a, (VALUES
        ('Tu capacidad de ahorro del 18% está en niveles saludables. ¡Bien hecho!'),
        ('Considera automatizar un ahorro fijo mensual para mantener este ritmo.'),
        ('Tu nivel de endeudamiento (45%) es manejable. Evita adquirir nuevas deudas.'),
        ('El gasto en alimentación representa el 35% del total. Busca opciones más económicas en supermercado para optimizarlo.')
    ) AS r(txt);

    -- Abril: SALUDABLE (ligero deterioro)
    WITH a AS (
        INSERT INTO analisis (usuario_id, perfil, probabilidad, ingreso_mensual, nivel_endeudamiento, frecuencia_ahorro, fecha_analisis)
        VALUES (v_uid, 'SALUDABLE', 0.6800, 18000.00, 48.00, 'BAJA', '2026-04-30 20:00:00')
        RETURNING id
    )
    INSERT INTO recomendaciones (analisis_id, texto)
    SELECT a.id, r.txt FROM a, (VALUES
        ('Tu perfil financiero sigue siendo saludable, aunque la deuda subió ligeramente al 48%.'),
        ('Intenta mantener el nivel de endeudamiento por debajo del 50%.'),
        ('El gasto en transporte aumentó este mes. Considera combinar traslados para reducirlo.'),
        ('Frecuencia de ahorro baja: establecer una transferencia automática el día de nómina puede ayudarte.')
    ) AS r(txt);

    -- Mayo: EN_OBSERVACION
    WITH a AS (
        INSERT INTO analisis (usuario_id, perfil, probabilidad, ingreso_mensual, nivel_endeudamiento, frecuencia_ahorro, fecha_analisis)
        VALUES (v_uid, 'EN_OBSERVACION', 0.6100, 18000.00, 52.00, 'BAJA', '2026-05-31 20:00:00')
        RETURNING id
    )
    INSERT INTO recomendaciones (analisis_id, texto)
    SELECT a.id, r.txt FROM a, (VALUES
        ('⚠️ Entraste en perfil EN OBSERVACIÓN. Tu deuda (52%) supera el umbral recomendado del 50%.'),
        ('Gasto en delivery (Uber Eats, Rappi, Didi Food) totalizó $1,620 este mes. Reducirlo a la mitad ahorraría ~$810.'),
        ('Revisa tus suscripciones digitales: Netflix + Disney+ + Spotify pueden representar un gasto innecesario duplicado.'),
        ('Prioridad: no asumir nuevas deudas hasta bajar el ratio de endeudamiento por debajo del 45%.')
    ) AS r(txt);

    -- Junio: EN_OBSERVACION
    WITH a AS (
        INSERT INTO analisis (usuario_id, perfil, probabilidad, ingreso_mensual, nivel_endeudamiento, frecuencia_ahorro, fecha_analisis)
        VALUES (v_uid, 'EN_OBSERVACION', 0.5800, 18000.00, 55.00, 'BAJA', '2026-06-30 20:00:00')
        RETURNING id
    )
    INSERT INTO recomendaciones (analisis_id, texto)
    SELECT a.id, r.txt FROM a, (VALUES
        ('Tu endeudamiento alcanzó el 55%. Llevas 2 meses consecutivos en perfil de observación.'),
        ('Alimentación concentra el 38% del gasto total. Es la categoría con mayor margen de ajuste.'),
        ('Una consulta de $800 este mes afectó tu capacidad de ahorro. Considera un seguro de gastos médicos.'),
        ('Meta sugerida: reducir gasto total en $1,500 el próximo mes para recuperar margen de ahorro.')
    ) AS r(txt);

    -- Julio: EN_OBSERVACION
    WITH a AS (
        INSERT INTO analisis (usuario_id, perfil, probabilidad, ingreso_mensual, nivel_endeudamiento, frecuencia_ahorro, fecha_analisis)
        VALUES (v_uid, 'EN_OBSERVACION', 0.5500, 18000.00, 58.00, 'BAJA', '2026-07-31 20:00:00')
        RETURNING id
    )
    INSERT INTO recomendaciones (analisis_id, texto)
    SELECT a.id, r.txt FROM a, (VALUES
        ('⚠️ Tercer mes consecutivo EN OBSERVACIÓN. Tu deuda escaló al 58%, acercándose al nivel crítico del 60%.'),
        ('El gasto en transporte ($2,360) aumentó 15% respecto al mes anterior. Evalúa opciones de transporte público.'),
        ('Tu ahorro neto este mes fue de solo $1,300 — el menor en 5 meses. Se necesita acción inmediata.'),
        ('Recomendación urgente: elabora un presupuesto semanal y establece un límite máximo de gasto por categoría.')
    ) AS r(txt);

    -- Agosto: EN_OBSERVACION (mes actual, análisis reciente)
    WITH a AS (
        INSERT INTO analisis (usuario_id, perfil, probabilidad, ingreso_mensual, nivel_endeudamiento, frecuencia_ahorro, fecha_analisis)
        VALUES (v_uid, 'EN_OBSERVACION', 0.5200, 18000.00, 60.00, 'BAJA', '2026-08-19 09:30:00')
        RETURNING id
    )
    INSERT INTO recomendaciones (analisis_id, texto)
    SELECT a.id, r.txt FROM a, (VALUES
        ('🚨 Tu nivel de endeudamiento llegó al 60% — en el umbral de alerta CRÍTICA. Requiere atención inmediata.'),
        ('Alimentación este mes: $5,450 (36% del ingreso). Walmart + Chedraui + delivery suman $3,700. Cocinar en casa puede reducirlo en un 40%.'),
        ('Transportes: $2,150 en gasolina + apps. Considera alternativas como bicicleta o transporte público 2-3 días por semana.'),
        ('Tu capacidad de ahorro actual es del 6.1% ($1,100). El mínimo recomendado es 10% ($1,800). Necesitas liberar $700 mensuales.'),
        ('Acciones prioritarias: 1) Eliminar una plataforma de streaming, 2) Reducir delivery a 2 veces por semana, 3) No adquirir nuevas deudas.')
    ) AS r(txt);

END $$;
