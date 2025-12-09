-- Script para crear los tipos enum necesarios para la tabla codigoverificacion
-- Ejecutar este script en la base de datos PostgreSQL de Render

-- Crear tipo enum para codigoverificacion_tipo
CREATE TYPE codigoverificacion_tipo AS ENUM ('RESET_CLAVE');

-- Crear tipo enum para codigoverificacion_estado
CREATE TYPE codigoverificacion_estado AS ENUM ('GENERADO', 'USADO', 'EXPIRADO');

-- Si la tabla ya existe con columnas VARCHAR, necesitas alterarla:
-- ALTER TABLE codigoverificacion 
--   ALTER COLUMN tipo TYPE codigoverificacion_tipo USING tipo::codigoverificacion_tipo,
--   ALTER COLUMN estado TYPE codigoverificacion_estado USING estado::codigoverificacion_estado;

