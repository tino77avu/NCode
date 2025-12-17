package com.admin.ncode.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicituddemo")
@Data
public class SolicitudDemo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solicitudid", nullable = false)
    private Long solicitudId;
    
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    @Column(name = "correo", length = 100, nullable = false)
    private String correo;
    
    @Column(name = "empresa", length = 200, nullable = false)
    private String empresa;
    
    @Column(name = "ruc", length = 20, nullable = false)
    private String ruc;
    
    @Column(name = "direccion", length = 300, nullable = false)
    private String direccion;
    
    @Column(name = "codigoid", insertable = true, updatable = true)
    private Long codigoId;
    
    @Column(name = "codigoverificacion", length = 12)
    private String codigoVerificacion;
    
    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "PENDIENTE";
    
    @Column(name = "ipcliente", length = 45)
    private String ipCliente;
    
    @Column(name = "fechasolicitud", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaSolicitud;
    
    @Column(name = "fechaprocesado")
    private LocalDateTime fechaProcesado;
    
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigoid", insertable = false, updatable = false)
    private CodigoVerificacion codigo;
    
    public enum EstadoSolicitud {
        PENDIENTE, PROCESADO, CANCELADO
    }
}

