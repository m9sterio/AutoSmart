package com.autosmart.repository;

import com.autosmart.model.PropostaVersao;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PropostaVersaoRepo extends JpaRepository<PropostaVersao, Long> {
    long countByPropostaId(Long id);
    List<PropostaVersao> findByPropostaIdOrderByVersaoDesc(Long id);
}
