package com.autosmart.repository;

import com.autosmart.model.Proposta;

import java.util.*;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropostaRepo extends JpaRepository<Proposta, Long> {
    long countByStatus(Proposta.Status s);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Proposta p where p.id = :id")
    Optional<Proposta> bloquear(@Param("id") Long id);
}
