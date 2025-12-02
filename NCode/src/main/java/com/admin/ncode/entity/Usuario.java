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
    @Column(name = "UsuarioID", nullable = false)
    private Long usuarioId;
    
    @Column(name = "Email", length = 160, nullable = false, unique = true)
    private String email;
    
    @Column(name = "HashPassword", length = 255, nullable = false)
    private String hashPassword;
    
    @Column(name = "Nombres", length = 100, nullable = false)
    private String nombres;
    
    @Column(name = "Apellidos", length = 120, nullable = false)
    private String apellidos;
    
    @Column(name = "Telefono", length = 30)
    private String telefono;
    
    @Column(name = "EsAdminGlobal", nullable = false)
    private Boolean esAdminGlobal = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", columnDefinition = "ENUM('ACTIVO', 'BLOQUEADO')")
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;
    
    @Column(name = "UltimoLogin")
    private LocalDateTime ultimoLogin;
    
    @Column(name = "FechaRegistro", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RolGlobalID", nullable = false)
    private RolGlobal rolGlobal;
    
    public enum EstadoUsuario {
        ACTIVO, BLOQUEADO
    }
}

