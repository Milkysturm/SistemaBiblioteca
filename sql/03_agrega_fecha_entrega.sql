-- =====================================================================
-- Script 3: agrega la fecha real de entrega a la tabla prestamos.
--
-- La pauta pide detectar el atraso en la devolucion y "dejar constancia"
-- de ello. La tabla original solo guarda la fecha de vencimiento
-- (fecha_devolucion), asi que se agrega fecha_entrega: el dia en que el
-- libro se devolvio de verdad. Si fecha_entrega es posterior al
-- vencimiento, el prestamo se devolvio con atraso.
--
-- Ejecutar UNA vez, despues de los scripts de creacion y poblamiento.
-- Los prestamos de ejemplo ya devueltos quedan con fecha_entrega en NULL.
-- =====================================================================
USE biblioteca;

ALTER TABLE prestamos
    ADD COLUMN fecha_entrega DATE NULL AFTER fecha_devolucion;
