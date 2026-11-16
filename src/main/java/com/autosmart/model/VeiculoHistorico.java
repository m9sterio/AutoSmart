package com.autosmart.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class VeiculoHistorico {
    @Id
    @GeneratedValue
    public Long id;
    @ManyToOne(optional = false) public Veiculo veiculo;
    @Column(length = 3500) public String dados;
    public String usuario, acao;
    public LocalDateTime data;
}
