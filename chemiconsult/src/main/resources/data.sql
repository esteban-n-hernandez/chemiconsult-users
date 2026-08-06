-- CLIENTE_ID debe ser nullable para VENCIMIENTO y COMPRA_INSUMOS (no siempre hay cliente)
ALTER TABLE "MUESTREO_AGENDADO" ALTER COLUMN "CLIENTE_ID" DROP NOT NULL;

-- Recrear constraint de TIPO para incluir VENCIMIENTO (añadido al enum después de la creación de la tabla)
ALTER TABLE "MUESTREO_AGENDADO" DROP CONSTRAINT IF EXISTS "MUESTREO_AGENDADO_TIPO_check";
ALTER TABLE "MUESTREO_AGENDADO" ADD CONSTRAINT "MUESTREO_AGENDADO_TIPO_check"
    CHECK ("TIPO" IN ('MUESTREO', 'COMPRA_INSUMOS', 'VENCIMIENTO', 'OTRO', 'DOCUMENTACION', 'VISITA_TECNICA', 'ANALISIS'));

-- Recrear constraint de TASK para incluir EN_REVISION
ALTER TABLE "TASK" DROP CONSTRAINT IF EXISTS "TASK_status_check";
ALTER TABLE "TASK" ADD CONSTRAINT "TASK_status_check"
    CHECK (status IN ('TODO', 'IN_PROGRESS', 'EN_REVISION', 'DONE'));

-- Recrear constraints de STOCK_ITEM para reflejar los enums actuales
ALTER TABLE "STOCK_ITEM" DROP CONSTRAINT IF EXISTS "STOCK_ITEM_nivel_check";
ALTER TABLE "STOCK_ITEM" DROP CONSTRAINT IF EXISTS "STOCK_ITEM_categoria_check";
ALTER TABLE "STOCK_ITEM" ADD CONSTRAINT "STOCK_ITEM_nivel_check"
    CHECK (nivel IN ('ALTO', 'MEDIO', 'BAJO'));
ALTER TABLE "STOCK_ITEM" ADD CONSTRAINT "STOCK_ITEM_categoria_check"
    CHECK (categoria IN ('REACTIVOS', 'SOLVENTES', 'MATERIAL_MUESTREO', 'MATERIAL_VIDRIO', 'OTROS'));

-- Cola de análisis: analista responsable por parámetro
ALTER TABLE "PARAMETRO" ADD COLUMN IF NOT EXISTS "RESPONSABLE_ID" BIGINT;

-- Cola de análisis: estado analizado por ítem (PENDIENTE=false, ANALIZADO=true)
ALTER TABLE "ANALISIS_PARAMETRO" ADD COLUMN IF NOT EXISTS "ANALIZADO" BOOLEAN NOT NULL DEFAULT FALSE;

-- Cola de análisis: migrar boolean ANALIZADO → enum ESTADO_ANALISIS
ALTER TABLE "ANALISIS_PARAMETRO" ADD COLUMN IF NOT EXISTS "ESTADO_ANALISIS" VARCHAR(50);
UPDATE "ANALISIS_PARAMETRO"
SET "ESTADO_ANALISIS" = CASE WHEN "ANALIZADO" = true THEN 'ANALIZADO' ELSE 'PENDIENTE' END
WHERE "ESTADO_ANALISIS" IS NULL;

INSERT INTO "NUMERADORES" (nombre, valor)
VALUES ('NUMERO_PRESUPUESTO', 0)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO resolucion (nombre, descripcion) VALUES
                                                 ('Res 336/06',          'Resolución 336/06 - Vuelco de efluentes'),
                                                 ('Res 283/19',          'Resolución 283/19 - Vuelco de efluentes'),
                                                 ('CAA',                 'Código Alimentario Argentino'),
                                                 ('Ley 19587 - Dec 351/79', 'Ley de Higiene y Seguridad en el Trabajo');