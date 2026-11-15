package com.autosmart.repository;

import com.autosmart.model.Alerta;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaRepo extends JpaRepository<Alerta, Long> {
    List<Alerta> findByConcluidoFalseOrderByData();
    long countByConcluidoFalse();
    java.util.Optional<Alerta> findByOrigemTipoAndOrigemId(String tipo, Long id);
}
