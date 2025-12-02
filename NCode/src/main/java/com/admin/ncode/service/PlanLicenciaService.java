package com.admin.ncode.service;

import com.admin.ncode.entity.PlanLicencia;
import com.admin.ncode.entity.PlanLicenciaDetalle;
import com.admin.ncode.repository.PlanLicenciaRepository;
import com.admin.ncode.repository.PlanLicenciaDetalleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanLicenciaService {

    @Autowired
    private PlanLicenciaRepository planLicenciaRepository;

    @Autowired
    private PlanLicenciaDetalleRepository planLicenciaDetalleRepository;

    public List<PlanLicencia> findAllPlanes() {
        List<Object[]> results = planLicenciaRepository.findAllPlansNative();
        
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        
        return results.stream().map(row -> {
            try {
                PlanLicencia plan = new PlanLicencia();
                plan.setPlanId(row[0] != null ? ((Number) row[0]).intValue() : null);
                plan.setNombre((String) row[1]);
                plan.setDescripcion((String) row[2]);
                plan.setPrecio(row[3] != null ? (BigDecimal) row[3] : null);
                plan.setTipo((String) row[4]);
                
                // Manejar Destacado que puede ser Boolean o Number
                if (row[5] != null) {
                    if (row[5] instanceof Boolean) {
                        plan.setDestacado((Boolean) row[5]);
                    } else if (row[5] instanceof Number) {
                        plan.setDestacado(((Number) row[5]).intValue() == 1);
                    } else {
                        plan.setDestacado(false);
                    }
                } else {
                    plan.setDestacado(false);
                }
                
                plan.setBoton((String) row[6]);
                return plan;
            } catch (Exception e) {
                return null;
            }
        }).filter(p -> p != null).collect(Collectors.toList());
    }

    public List<PlanLicenciaDetalle> findAllDetalles() {
        List<Object[]> results = planLicenciaDetalleRepository.findAllDetallesNative();
        
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        
        return results.stream().map(row -> {
            try {
                PlanLicenciaDetalle detalle = new PlanLicenciaDetalle();
                detalle.setDetalleId(row[0] != null ? ((Number) row[0]).intValue() : null);
                detalle.setPlanId(row[1] != null ? ((Number) row[1]).intValue() : null);
                detalle.setDescripcion((String) row[2]);
                return detalle;
            } catch (Exception e) {
                return null;
            }
        }).filter(d -> d != null).collect(Collectors.toList());
    }

    public Map<Integer, List<PlanLicenciaDetalle>> getDetallesPorPlan() {
        List<PlanLicenciaDetalle> detalles = findAllDetalles();
        return detalles.stream()
                .collect(Collectors.groupingBy(PlanLicenciaDetalle::getPlanId));
    }
}

