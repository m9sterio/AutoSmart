package com.autosmart.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class RecomendacaoHistorico {
    @Id
    @GeneratedValue
    public Long id;
    @ManyToOne(optional = false) public PerfilCompra perfil;
    @ManyToOne(optional = false) public Veiculo veiculo;
    public int classificacao, pontuacao;
    public String usuario, veiculoDescricao;
    @Column(length = 3000) public String perfilResumo;
    @Column(length = 4000) public String positivos;
    @Column(length = 4000) public String atencao;
    @Column(length = 4000) public String semCorrespondencia;
    public LocalDateTime data;
}
