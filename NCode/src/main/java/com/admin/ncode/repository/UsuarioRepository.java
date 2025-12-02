package com.admin.ncode.repository;

import com.admin.ncode.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findAllByOrderByUsuarioIdAsc();
    Optional<Usuario> findByEmail(String email);
}

