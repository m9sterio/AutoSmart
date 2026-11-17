package com.autosmart.repository;

import com.autosmart.model.Cliente;

import java.util.*;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepo extends JpaRepository<Cliente, Long> {
    List<Cliente> findByAtivoTrue();
    boolean existsByCpf(String cpf);
    Optional<Cliente> findByCpf(String cpf);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from Cliente c where c.id = :id")
    Optional<Cliente> bloquear(@Param("id") Long id);
}
