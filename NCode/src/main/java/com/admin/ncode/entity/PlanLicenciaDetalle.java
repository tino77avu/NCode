package com.admin.ncode.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "planlicenciadetalle")
@Data
public class PlanLicenciaDetalle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detalleid")
    private Integer detalleId;
    
    @Column(name = "planid")
    private Integer planId;
    
    @Column(name = "descripcion", length = 255)
    private String descripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planid", insertable = false, updatable = false)
    private PlanLicencia planLicencia;
}

