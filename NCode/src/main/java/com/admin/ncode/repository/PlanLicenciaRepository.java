package com.admin.ncode.repository;

import com.admin.ncode.entity.PlanLicencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanLicenciaRepository extends JpaRepository<PlanLicencia, Integer> {
    @Query(value = "SELECT planid, nombre, descripcion, precio, tipo, destacado, boton FROM planlicencia ORDER BY planid ASC", nativeQuery = true)
    List<Object[]> findAllPlansNative();
}

