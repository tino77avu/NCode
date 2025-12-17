-- Script para sincronizar la secuencia de empresaid con el valor máximo actual
-- Ejecutar este script en la base de datos PostgreSQL si hay errores de llave duplicada

-- Obtener el valor máximo actual de empresaid
-- Luego actualizar la secuencia para que el próximo valor sea mayor

SELECT setval('empresa_empresaid_seq', COALESCE((SELECT MAX(empresaid) FROM empresa), 1), true);

-- Verificar que la secuencia está correcta
-- (Opcional: ejecutar esta consulta para verificar)
-- SELECT last_value FROM empresa_empresaid_seq;

