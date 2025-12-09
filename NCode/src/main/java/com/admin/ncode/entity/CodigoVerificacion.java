package com.admin.ncode.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "codigoverificacion")
@Data
public class CodigoVerificacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigoid", nullable = false)
    private Long codigoId;
    
    @Column(name = "usuarioid", nullable = false)
    private Long usuarioId;
    
    @Column(name = "empresaid", nullable = false)
    private Long empresaId;
    
    @Column(name = "codigo", length = 12, nullable = false)
    private String codigo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, columnDefinition = "codigoverificacion_tipo")
    private TipoCodigo tipo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "codigoverificacion_estado")
    private EstadoCodigo estado = EstadoCodigo.GENERADO;
    
    @Column(name = "intentosusados", nullable = false)
    private Integer intentosUsados = 0;
    
    @Column(name = "maxintentos", nullable = false)
    private Integer maxIntentos = 0;
    
    @Column(name = "fechacreacion", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fechaexpiracion")
    private LocalDateTime fechaExpiracion;
    
    @Column(name = "usadoen")
    private LocalDateTime usadoEn;
    
    @Column(name = "ipgeneracion", length = 45)
    private String ipGeneracion;
    
    public enum TipoCodigo {
        RESET_CLAVE
    }
    
    public enum EstadoCodigo {
        GENERADO, USADO, EXPIRADO
    }
}

