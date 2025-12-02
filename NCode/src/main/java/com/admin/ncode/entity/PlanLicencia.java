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
    @Column(name = "PlanID", nullable = false)
    private Integer planId;
    
    @Column(name = "Nombre", length = 50)
    private String nombre;
    
    @Column(name = "Descripcion", length = 255)
    private String descripcion;
    
    @Column(name = "Precio", precision = 10, scale = 2, nullable = true)
    private BigDecimal precio;
    
    @Column(name = "Tipo", length = 50)
    private String tipo;
    
    @Column(name = "Destacado")
    private Boolean destacado;
    
    @Column(name = "Boton", length = 45, nullable = true)
    private String boton;
}

