package com.admin.ncode.repository;

import com.admin.ncode.entity.RolGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RolGlobalRepository extends JpaRepository<RolGlobal, Integer> {
    List<RolGlobal> findAllByOrderByRolGlobalIdAsc();
}

