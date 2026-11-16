package com.autosmart.model;

import java.math.BigDecimal;
import java.time.*;

import jakarta.persistence.*;

@Entity
public class PropostaVersao {
    @Id
    @GeneratedValue
    public Long id;
    @ManyToOne(optional = false) public Proposta proposta;
    public long versao;
    @Enumerated(EnumType.STRING)
    public Proposta.Status status;
    public BigDecimal valor, desconto, precoBase;
    public String acessorios, responsavel, usuario, acao;
    public LocalDate validade;
    public LocalDateTime data;
}
