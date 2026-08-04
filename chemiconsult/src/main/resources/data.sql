-- CLIENTE_ID debe ser nullable para VENCIMIENTO y COMPRA_INSUMOS (no siempre hay cliente)
ALTER TABLE "MUESTREO_AGENDADO" ALTER COLUMN "CLIENTE_ID" DROP NOT NULL;

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

INSERT INTO resolucion (nombre, descripcion) VALUES
                                                 ('Res 336/06',          'Resolución 336/06 - Vuelco de efluentes'),
                                                 ('Res 283/19',          'Resolución 283/19 - Vuelco de efluentes'),
                                                 ('CAA',                 'Código Alimentario Argentino'),
                                                 ('Ley 19587 - Dec 351/79', 'Ley de Higiene y Seguridad en el Trabajo');