package com.autosmart.repository;

import com.autosmart.model.PerfilCompra;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepo extends JpaRepository<PerfilCompra, Long> {
    List<PerfilCompra> findByClienteIdOrderByIdDesc(Long id);
}
