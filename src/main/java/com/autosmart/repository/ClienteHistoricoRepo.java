package com.autosmart.repository;

import com.autosmart.model.ClienteHistorico;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteHistoricoRepo extends JpaRepository<ClienteHistorico, Long> {
    List<ClienteHistorico> findByClienteIdOrderByDataDesc(Long id);
}
