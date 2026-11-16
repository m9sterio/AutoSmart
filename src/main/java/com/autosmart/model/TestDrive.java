package com.autosmart.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class TestDrive {
    @Id
    @GeneratedValue
    public Long id;
    @ManyToOne
    public Veiculo veiculo;
    @ManyToOne
    public Cliente cliente;
    public String vendedor;
    public LocalDateTime inicio;
}
