package com.admin.ncode.repository;

import com.admin.ncode.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findAllByOrderByUsuarioIdAsc();
    Optional<Usuario> findByEmail(String email);
    
    @Query(value = "SELECT * FROM usuario WHERE email = :email AND estado::text = :estado", nativeQuery = true)
    Optional<Usuario> findByEmailAndEstado(@Param("email") String email, @Param("estado") String estado);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE usuario SET hashpassword = :hashPassword WHERE usuarioid = :usuarioId", nativeQuery = true)
    void updatePassword(@Param("usuarioId") Long usuarioId, @Param("hashPassword") String hashPassword);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE usuario SET ultimologin = :ultimoLogin WHERE usuarioid = :usuarioId", nativeQuery = true)
    void updateUltimoLogin(@Param("usuarioId") Long usuarioId, @Param("ultimoLogin") LocalDateTime ultimoLogin);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE usuario SET estado = CAST(:estado AS usuario_estado) WHERE usuarioid = :usuarioId", nativeQuery = true)
    void updateEstado(@Param("usuarioId") Long usuarioId, @Param("estado") String estado);
}

