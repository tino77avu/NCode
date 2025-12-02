package com.admin.ncode.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "rolglobal")
@Data
public class RolGlobal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RolGlobalID", nullable = false)
    private Integer rolGlobalId;
    
    @Column(name = "NombreRol", length = 50, nullable = false)
    private String nombreRol;
    
    @Column(name = "Descripcion", length = 150)
    private String descripcion;
}

