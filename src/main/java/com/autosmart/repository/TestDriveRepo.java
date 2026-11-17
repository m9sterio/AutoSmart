package com.autosmart.repository;

import com.autosmart.model.TestDrive;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TestDriveRepo extends JpaRepository<TestDrive, Long> {
    @Query("select count(t) > 0 from TestDrive t where t.veiculo.id = :id and t.inicio > :a and t.inicio < :b")
    boolean conflitoVeiculo(@Param("id") Long id, @Param("a") LocalDateTime a, @Param("b") LocalDateTime b);
}
