package com.autosmart.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
public class PerfilCompra {
    @Id
    @GeneratedValue
    public Long id;
    @ManyToOne
    public Cliente cliente;
    public BigDecimal orcamento;
    public String finalidade, categoria, cambio;
    public Integer ocupantes;
    public Double consumoMin;
    @Column(length = 1500)
    public String prioridades;
}
