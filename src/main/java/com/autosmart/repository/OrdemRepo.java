package com.autosmart.repository;

import com.autosmart.model.OrdemServico;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemRepo extends JpaRepository<OrdemServico, Long> {
    long countByStatus(OrdemServico.Status status);
}
