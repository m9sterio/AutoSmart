package com.autosmart.repository;

import com.autosmart.model.Usuario;

import java.util.*;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepo extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String e);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select u from Usuario u where u.email = :email")
    Optional<Usuario> bloquearPorEmail(@Param("email") String email);
}
