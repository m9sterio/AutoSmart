package com.autosmart.repository;

import com.autosmart.model.Oportunidade;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OportunidadeRepo extends JpaRepository<Oportunidade, Long> {
    long countByEtapa(Oportunidade.Etapa etapa);
}
