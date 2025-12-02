package com.admin.ncode.repository;

import com.admin.ncode.entity.PlanLicencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanLicenciaRepository extends JpaRepository<PlanLicencia, Integer> {
    @Query(value = "SELECT PlanID, Nombre, Descripcion, Precio, Tipo, Destacado, Boton FROM planlicencia ORDER BY PlanID ASC", nativeQuery = true)
    List<Object[]> findAllPlansNative();
}

