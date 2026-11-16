package com.autosmart.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class OrdemServico {
    public enum Status {
        ABERTA, EM_ANDAMENTO, ENCERRADA
    }
    @Id
    @GeneratedValue
    public Long id;
    @ManyToOne
    public Cliente cliente;
    @ManyToOne
    public Veiculo veiculo;
    public String descricao, diagnostico, atividades, pecas, responsavel;
    public BigDecimal valor;
    public LocalDate abertura;
    @Enumerated(EnumType.STRING)
    public Status status = Status.ABERTA;
}
