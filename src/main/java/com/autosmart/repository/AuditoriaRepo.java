package com.autosmart.repository;

import com.autosmart.model.Auditoria;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepo extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findTop20ByOrderByDataDesc();
    boolean existsByAcao(String a);
}
