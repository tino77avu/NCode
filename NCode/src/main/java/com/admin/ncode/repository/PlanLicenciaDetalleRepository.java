package com.admin.ncode.repository;

import com.admin.ncode.entity.PlanLicenciaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanLicenciaDetalleRepository extends JpaRepository<PlanLicenciaDetalle, Integer> {
    @Query(value = "SELECT detalleid, planid, descripcion FROM planlicenciadetalle ORDER BY planid ASC, detalleid ASC", nativeQuery = true)
    List<Object[]> findAllDetallesNative();
    
    List<PlanLicenciaDetalle> findByPlanIdOrderByDetalleIdAsc(Integer planId);
}

