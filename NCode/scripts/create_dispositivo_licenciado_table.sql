-- Script para crear la tabla de dispositivos licenciados
-- Esta tabla almacena información sobre los dispositivos que tienen licencias activas
-- Ejecutar este script en la base de datos PostgreSQL

-- Crear tabla dispositivolicenciado
CREATE TABLE IF NOT EXISTS dispositivolicenciado (
    dispositivid BIGSERIAL PRIMARY KEY,
    empresaid BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    macaddress VARCHAR(17) NOT NULL,
    tipo VARCHAR(50) NOT NULL DEFAULT 'DESKTOP',
    sistemaoperativo VARCHAR(100),
    versionso VARCHAR(50),
    versionsoftware VARCHAR(50),
    ipdispositivo VARCHAR(45),
    estadolicencia VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fechainstalacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechavencimiento TIMESTAMP,
    fechaultimaconexion TIMESTAMP,
    ultimaipconexion VARCHAR(45),
    licenciaid VARCHAR(100),
    codigoactivacion VARCHAR(50),
    cantidadusuarios INTEGER DEFAULT 1,
    fechacreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechamodificacion TIMESTAMP,
    usuariocreador BIGINT,
    usuariomodificador BIGINT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    notas TEXT,
    CONSTRAINT fk_dispositivo_empresa FOREIGN KEY (empresaid) 
        REFERENCES empresa(empresaid) ON DELETE RESTRICT,
    CONSTRAINT fk_dispositivo_usuariocreador FOREIGN KEY (usuariocreador) 
        REFERENCES usuario(usuarioid) ON DELETE SET NULL,
    CONSTRAINT fk_dispositivo_usuariomodificador FOREIGN KEY (usuariomodificador) 
        REFERENCES usuario(usuarioid) ON DELETE SET NULL,
    CONSTRAINT uq_dispositivo_mac_empresa UNIQUE (macaddress, empresaid),
    CONSTRAINT chk_macaddress_format CHECK (macaddress ~ '^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$'),
    CONSTRAINT chk_fechavencimiento_mayor_instalacion CHECK (fechavencimiento IS NULL OR fechavencimiento > fechainstalacion)
);

-- Crear índices para mejorar el rendimiento de búsquedas
CREATE INDEX IF NOT EXISTS idx_dispositivo_empresaid ON dispositivolicenciado(empresaid);
CREATE INDEX IF NOT EXISTS idx_dispositivo_macaddress ON dispositivolicenciado(macaddress);
CREATE INDEX IF NOT EXISTS idx_dispositivo_estadolicencia ON dispositivolicenciado(estadolicencia);
CREATE INDEX IF NOT EXISTS idx_dispositivo_activo ON dispositivolicenciado(activo);
CREATE INDEX IF NOT EXISTS idx_dispositivo_fechainstalacion ON dispositivolicenciado(fechainstalacion);
CREATE INDEX IF NOT EXISTS idx_dispositivo_fechavencimiento ON dispositivolicenciado(fechavencimiento);
CREATE INDEX IF NOT EXISTS idx_dispositivo_fechaultimaconexion ON dispositivolicenciado(fechaultimaconexion);
CREATE INDEX IF NOT EXISTS idx_dispositivo_tipo ON dispositivolicenciado(tipo);
CREATE INDEX IF NOT EXISTS idx_dispositivo_licenciaid ON dispositivolicenciado(licenciaid);

-- Índice compuesto para búsquedas frecuentes
CREATE INDEX IF NOT EXISTS idx_dispositivo_empresa_activo ON dispositivolicenciado(empresaid, activo);
CREATE INDEX IF NOT EXISTS idx_dispositivo_empresa_estado ON dispositivolicenciado(empresaid, estadolicencia);

-- Comentarios en la tabla y columnas
COMMENT ON TABLE dispositivolicenciado IS 'Tabla para almacenar información de dispositivos que tienen licencias activas del sistema NCOD3';
COMMENT ON COLUMN dispositivolicenciado.dispositivid IS 'ID único del dispositivo licenciado';
COMMENT ON COLUMN dispositivolicenciado.empresaid IS 'ID de la empresa propietaria del dispositivo (FK a empresa)';
COMMENT ON COLUMN dispositivolicenciado.nombre IS 'Nombre descriptivo del dispositivo';
COMMENT ON COLUMN dispositivolicenciado.descripcion IS 'Descripción adicional del dispositivo';
COMMENT ON COLUMN dispositivolicenciado.macaddress IS 'Dirección MAC del dispositivo (formato: XX:XX:XX:XX:XX:XX o XX-XX-XX-XX-XX-XX)';
COMMENT ON COLUMN dispositivolicenciado.tipo IS 'Tipo de dispositivo: DESKTOP, SERVER, LAPTOP, MOBILE, TABLET, etc.';
COMMENT ON COLUMN dispositivolicenciado.sistemaoperativo IS 'Sistema operativo del dispositivo (ej: Windows 11, Linux Ubuntu, macOS)';
COMMENT ON COLUMN dispositivolicenciado.versionso IS 'Versión del sistema operativo';
COMMENT ON COLUMN dispositivolicenciado.versionsoftware IS 'Versión del software NCOD3 instalado';
COMMENT ON COLUMN dispositivolicenciado.ipdispositivo IS 'Dirección IP del dispositivo';
COMMENT ON COLUMN dispositivolicenciado.estadolicencia IS 'Estado de la licencia: ACTIVO, VENCIDO, SUSPENDIDO, BLOQUEADO, INACTIVO';
COMMENT ON COLUMN dispositivolicenciado.fechainstalacion IS 'Fecha y hora en que se instaló y activó la licencia en el dispositivo';
COMMENT ON COLUMN dispositivolicenciado.fechavencimiento IS 'Fecha y hora de vencimiento de la licencia (NULL si es permanente)';
COMMENT ON COLUMN dispositivolicenciado.fechaultimaconexion IS 'Fecha y hora de la última conexión del dispositivo';
COMMENT ON COLUMN dispositivolicenciado.ultimaipconexion IS 'Última dirección IP desde donde se conectó el dispositivo';
COMMENT ON COLUMN dispositivolicenciado.licenciaid IS 'Identificador único de la licencia asignada';
COMMENT ON COLUMN dispositivolicenciado.codigoactivacion IS 'Código de activación utilizado para activar la licencia';
COMMENT ON COLUMN dispositivolicenciado.cantidadusuarios IS 'Número de usuarios permitidos en este dispositivo';
COMMENT ON COLUMN dispositivolicenciado.fechacreacion IS 'Fecha y hora de creación del registro';
COMMENT ON COLUMN dispositivolicenciado.fechamodificacion IS 'Fecha y hora de última modificación del registro';
COMMENT ON COLUMN dispositivolicenciado.usuariocreador IS 'ID del usuario que creó el registro (FK a usuario)';
COMMENT ON COLUMN dispositivolicenciado.usuariomodificador IS 'ID del usuario que modificó el registro por última vez (FK a usuario)';
COMMENT ON COLUMN dispositivolicenciado.activo IS 'Indica si el registro está activo (TRUE) o inactivo/eliminado lógicamente (FALSE)';
COMMENT ON COLUMN dispositivolicenciado.notas IS 'Notas adicionales sobre el dispositivo o la licencia';

-- Función para actualizar automáticamente fechamodificacion
CREATE OR REPLACE FUNCTION update_dispositivo_fechamodificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fechamodificacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar fechamodificacion automáticamente
CREATE TRIGGER trigger_update_dispositivo_fechamodificacion
    BEFORE UPDATE ON dispositivolicenciado
    FOR EACH ROW
    EXECUTE FUNCTION update_dispositivo_fechamodificacion();

-- Trigger para actualizar fechaultimaconexion cuando cambia ultimaipconexion
CREATE OR REPLACE FUNCTION update_dispositivo_ultimaconexion()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.ultimaipconexion IS DISTINCT FROM OLD.ultimaipconexion THEN
        NEW.fechaultimaconexion = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_dispositivo_ultimaconexion
    BEFORE UPDATE ON dispositivolicenciado
    FOR EACH ROW
    WHEN (NEW.ultimaipconexion IS DISTINCT FROM OLD.ultimaipconexion)
    EXECUTE FUNCTION update_dispositivo_ultimaconexion();

