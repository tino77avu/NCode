package com.admin.ncode.repository;

import com.admin.ncode.entity.CodigoVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO codigoverificacion (usuarioid, empresaid, codigo, tipo, estado, intentosusados, maxintentos, fechaexpiracion, ipgeneracion) " +
                   "VALUES (:usuarioid, :empresaid, :codigo, CAST(:tipo AS codigoverificacion_tipo), CAST(:estado AS codigoverificacion_estado), :intentosusados, :maxintentos, :fechaexpiracion, :ipgeneracion)", 
           nativeQuery = true)
    void insertCodigoVerificacion(@Param("usuarioid") Long usuarioId,
                                  @Param("empresaid") Long empresaId,
                                  @Param("codigo") String codigo,
                                  @Param("tipo") String tipo,
                                  @Param("estado") String estado,
                                  @Param("intentosusados") Integer intentosUsados,
                                  @Param("maxintentos") Integer maxIntentos,
                                  @Param("fechaexpiracion") LocalDateTime fechaExpiracion,
                                  @Param("ipgeneracion") String ipGeneracion);
    
    @Query(value = "SELECT cv.* FROM codigoverificacion cv " +
                   "WHERE cv.usuarioid = :usuarioId AND cv.codigo = :codigo " +
                   "AND cv.tipo::text = 'RESET_CLAVE' " +
                   "ORDER BY cv.fechacreacion DESC LIMIT 1", 
           nativeQuery = true)
    Optional<CodigoVerificacion> findByUsuarioIdAndCodigo(@Param("usuarioId") Long usuarioId, @Param("codigo") String codigo);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE codigoverificacion SET estado = CAST(:estado AS codigoverificacion_estado), usadoen = :usadoEn WHERE codigoid = :codigoId", nativeQuery = true)
    void marcarComoUsado(@Param("codigoId") Long codigoId, @Param("estado") String estado, @Param("usadoEn") LocalDateTime usadoEn);
}

