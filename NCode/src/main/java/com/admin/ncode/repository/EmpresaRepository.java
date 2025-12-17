package com.admin.ncode.repository;

import com.admin.ncode.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    List<Empresa> findAllByOrderByEmpresaIdAsc();
    
    Optional<Empresa> findByRuc(String ruc);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE empresa SET estado = CAST(:estado AS empresa_estado) WHERE empresaid = :empresaId", nativeQuery = true)
    void updateEstado(@Param("empresaId") Long empresaId, @Param("estado") String estado);
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO empresa (ruc, razonsocial, nombrecomercial, pais, estado) " +
                   "VALUES (:ruc, :razonsocial, :nombrecomercial, :pais, CAST(:estado AS empresa_estado))", 
           nativeQuery = true)
    void insertEmpresa(@Param("ruc") String ruc,
                       @Param("razonsocial") String razonSocial,
                       @Param("nombrecomercial") String nombreComercial,
                       @Param("pais") String pais,
                       @Param("estado") String estado);
}

