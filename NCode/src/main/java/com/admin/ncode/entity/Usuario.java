package com.admin.ncode.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuarioid", nullable = false)
    private Long usuarioId;
    
    @Column(name = "email", length = 160, nullable = false, unique = true)
    private String email;
    
    @Column(name = "hashpassword", length = 255, nullable = false)
    private String hashPassword;
    
    @Column(name = "nombres", length = 100, nullable = false)
    private String nombres;
    
    @Column(name = "apellidos", length = 120, nullable = false)
    private String apellidos;
    
    @Column(name = "telefono", length = 30)
    private String telefono;
    
    @Column(name = "esadminglobal", nullable = false)
    private Boolean esAdminGlobal = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;
    
    @Column(name = "ultimologin")
    private LocalDateTime ultimoLogin;
    
    @Column(name = "fecharegistro", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rolgobalid", nullable = false)
    private RolGlobal rolGlobal;
    
    public enum EstadoUsuario {
        ACTIVO, BLOQUEADO
    }
}

