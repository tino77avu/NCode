package com.admin.ncode.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "planlicencia")
@Data
public class PlanLicencia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planid", nullable = false)
    private Integer planId;
    
    @Column(name = "nombre", length = 50)
    private String nombre;
    
    @Column(name = "descripcion", length = 255)
    private String descripcion;
    
    @Column(name = "precio", precision = 10, scale = 2, nullable = true)
    private BigDecimal precio;
    
    @Column(name = "tipo", length = 50)
    private String tipo;
    
    @Column(name = "destacado")
    private Boolean destacado;
    
    @Column(name = "boton", length = 45, nullable = true)
    private String boton;
}

