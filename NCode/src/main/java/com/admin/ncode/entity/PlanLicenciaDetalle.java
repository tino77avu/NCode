package com.admin.ncode.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "planlicenciadetalle")
@Data
public class PlanLicenciaDetalle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DetalleID")
    private Integer detalleId;
    
    @Column(name = "PlanID")
    private Integer planId;
    
    @Column(name = "Descripcion", length = 255)
    private String descripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PlanID", insertable = false, updatable = false)
    private PlanLicencia planLicencia;
}

