package com.admin.ncode.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "empresa")
@Data
public class Empresa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empresaid", nullable = false)
    private Long empresaId;
    
    @Column(name = "ruc", length = 15, nullable = false)
    private String ruc;
    
    @Column(name = "razonsocial", length = 150, nullable = false)
    private String razonSocial;
    
    @Column(name = "nombrecomercial", length = 150)
    private String nombreComercial;
    
    @Column(name = "pais", length = 60)
    private String pais;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "empresa_estado")
    private EstadoEmpresa estado;
    
    @Column(name = "fechacreacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    public enum EstadoEmpresa {
        ACTIVA, SUSPENDIDA, BAJA
    }
}

