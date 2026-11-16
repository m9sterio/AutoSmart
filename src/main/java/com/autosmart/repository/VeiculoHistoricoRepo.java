package com.autosmart.repository;

import com.autosmart.model.VeiculoHistorico;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoHistoricoRepo extends JpaRepository<VeiculoHistorico, Long> {
    List<VeiculoHistorico> findByVeiculoIdOrderByDataDesc(Long id);
}
