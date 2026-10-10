-- =====================================================================
-- Script 3: agrega la fecha real de entrega a la tabla prestamos.
--
-- La pauta pide detectar el atraso en la devolucion y "dejar constancia"
-- de ello. La tabla original solo guarda la fecha de vencimiento
-- (fecha_devolucion), asi que se agrega fecha_entrega: el dia en que el
-- libro se devolvio de verdad. Si fecha_entrega es posterior al
-- vencimiento, el prestamo se devolvio con atraso.
--
-- Ejecutar despues de los scripts de creacion y poblamiento. Se puede
-- ejecutar mas de una vez: si la columna ya existe, no hace nada.
-- (La aplicacion tambien agrega la columna sola al iniciar, si falta.)
-- Los prestamos de ejemplo ya devueltos quedan con fecha_entrega en NULL.
-- =====================================================================
USE biblioteca;

SET @existe := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = 'biblioteca'
                  AND TABLE_NAME = 'prestamos'
                  AND COLUMN_NAME = 'fecha_entrega');

SET @sentencia := IF(@existe = 0,
    'ALTER TABLE prestamos ADD COLUMN fecha_entrega DATE NULL AFTER fecha_devolucion',
    'SELECT ''La columna fecha_entrega ya existe'' AS aviso');

PREPARE paso FROM @sentencia;
EXECUTE paso;
DEALLOCATE PREPARE paso;
