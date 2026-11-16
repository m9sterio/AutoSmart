package com.autosmart.repository;

import com.autosmart.model.RecomendacaoHistorico;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecomendacaoHistoricoRepo extends JpaRepository<RecomendacaoHistorico, Long> {
    List<RecomendacaoHistorico> findByPerfilIdOrderByDataDescClassificacaoAsc(Long id);
}
