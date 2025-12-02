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
    @Column(name = "EmpresaID", nullable = false)
    private Long empresaId;
    
    @Column(name = "RUC", length = 15, nullable = false)
    private String ruc;
    
    @Column(name = "RazonSocial", length = 150, nullable = false)
    private String razonSocial;
    
    @Column(name = "NombreComercial", length = 150)
    private String nombreComercial;
    
    @Column(name = "Pais", length = 60)
    private String pais;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", columnDefinition = "ENUM('ACTIVA', 'SUSPENDIDA', 'BAJA')")
    private EstadoEmpresa estado;
    
    @Column(name = "FechaCreacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    public enum EstadoEmpresa {
        ACTIVA, SUSPENDIDA, BAJA
    }
}

