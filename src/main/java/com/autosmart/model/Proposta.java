package com.autosmart.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class Proposta {
    public enum Status {
        ABERTA, APROVADA, RECUSADA
    }
    @Id
    @GeneratedValue
    public Long id;
    @ManyToOne
    public Cliente cliente;
    @ManyToOne
    public Veiculo veiculo;
    public BigDecimal valor, desconto, precoBase;
    public String acessorios, responsavel;
    public LocalDate validade;
    @Enumerated(EnumType.STRING)
    public Status status = Status.ABERTA;
}
