package com.autosmart.repository;

import com.autosmart.model.Veiculo;

import java.util.*;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VeiculoRepo extends JpaRepository<Veiculo, Long> {
    List<Veiculo> findByStatus(Veiculo.Status s);
    long countByStatus(Veiculo.Status s);
    boolean existsByModeloAndVersao(String m, String v);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select v from Veiculo v where v.id = :id")
    Optional<Veiculo> bloquear(@Param("id") Long id);
}
