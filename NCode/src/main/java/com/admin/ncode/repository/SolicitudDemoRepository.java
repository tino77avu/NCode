package com.admin.ncode.repository;

import com.admin.ncode.entity.SolicitudDemo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudDemoRepository extends JpaRepository<SolicitudDemo, Long> {
    
    List<SolicitudDemo> findByCorreoOrderByFechaSolicitudDesc(String correo);
    
    List<SolicitudDemo> findByRucOrderByFechaSolicitudDesc(String ruc);
    
    List<SolicitudDemo> findByEstadoOrderByFechaSolicitudDesc(String estado);
    
    Optional<SolicitudDemo> findByCodigoVerificacion(String codigoVerificacion);
    
    @Query("SELECT s FROM SolicitudDemo s WHERE s.codigoId = :codigoId")
    Optional<SolicitudDemo> findByCodigoId(@Param("codigoId") Long codigoId);
}

