-- Script para crear la tabla de solicitudes de demo
-- Ejecutar este script en la base de datos PostgreSQL
-- IMPORTANTE: Antes de ejecutar este script, asegúrate de ejecutar add_demo_to_enum.sql

-- Crear tabla solicituddemo
CREATE TABLE IF NOT EXISTS solicituddemo (
    solicitudid BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL,
    empresa VARCHAR(200) NOT NULL,
    ruc VARCHAR(20) NOT NULL,
    direccion VARCHAR(300) NOT NULL,
    codigoid BIGINT,
    codigoverificacion VARCHAR(12),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    ipcliente VARCHAR(45),
    fechasolicitud TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechaprocesado TIMESTAMP,
    notas TEXT,
    CONSTRAINT fk_solicituddemo_codigo FOREIGN KEY (codigoid) 
        REFERENCES codigoverificacion(codigoid) ON DELETE SET NULL
);

-- Crear índices para mejorar el rendimiento de búsquedas
CREATE INDEX IF NOT EXISTS idx_solicituddemo_correo ON solicituddemo(correo);
CREATE INDEX IF NOT EXISTS idx_solicituddemo_ruc ON solicituddemo(ruc);
CREATE INDEX IF NOT EXISTS idx_solicituddemo_estado ON solicituddemo(estado);
CREATE INDEX IF NOT EXISTS idx_solicituddemo_fechasolicitud ON solicituddemo(fechasolicitud);
CREATE INDEX IF NOT EXISTS idx_solicituddemo_codigoid ON solicituddemo(codigoid);

-- Comentarios en la tabla y columnas
COMMENT ON TABLE solicituddemo IS 'Tabla para almacenar las solicitudes de demo de NCOD3';
COMMENT ON COLUMN solicituddemo.solicitudid IS 'ID único de la solicitud de demo';
COMMENT ON COLUMN solicituddemo.nombre IS 'Nombre completo del solicitante';
COMMENT ON COLUMN solicituddemo.correo IS 'Correo electrónico del solicitante';
COMMENT ON COLUMN solicituddemo.empresa IS 'Nombre de la empresa del solicitante';
COMMENT ON COLUMN solicituddemo.ruc IS 'RUC de la empresa';
COMMENT ON COLUMN solicituddemo.direccion IS 'Dirección de la empresa';
COMMENT ON COLUMN solicituddemo.codigoid IS 'ID del código de verificación asociado (FK a codigoverificacion)';
COMMENT ON COLUMN solicituddemo.codigoverificacion IS 'Código de verificación generado (duplicado para fácil acceso)';
COMMENT ON COLUMN solicituddemo.estado IS 'Estado de la solicitud: PENDIENTE, PROCESADO, CANCELADO';
COMMENT ON COLUMN solicituddemo.ipcliente IS 'IP del cliente que realizó la solicitud';
COMMENT ON COLUMN solicituddemo.fechasolicitud IS 'Fecha y hora en que se realizó la solicitud';
COMMENT ON COLUMN solicituddemo.fechaprocesado IS 'Fecha y hora en que se procesó la solicitud';
COMMENT ON COLUMN solicituddemo.notas IS 'Notas adicionales sobre la solicitud';

