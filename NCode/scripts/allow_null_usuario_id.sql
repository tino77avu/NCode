-- Script para permitir valores NULL en la columna usuarioid de codigoverificacion
-- Esto es necesario para las solicitudes de demo que no tienen un usuario asociado
-- Ejecutar este script en la base de datos PostgreSQL

-- Modificar la columna usuarioid para permitir NULL
ALTER TABLE codigoverificacion ALTER COLUMN usuarioid DROP NOT NULL;

-- Verificar que el cambio se aplicó correctamente
-- (Opcional: ejecutar esta consulta para verificar)
-- SELECT column_name, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'codigoverificacion' AND column_name = 'usuarioid';

