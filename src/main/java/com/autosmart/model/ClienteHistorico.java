package com.autosmart.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class ClienteHistorico {
    @Id
    @GeneratedValue
    public Long id;
    @ManyToOne(optional = false) public Cliente cliente;
    public String nome, telefone, email, cidade, usuario, acao;
    public boolean ativo;
    public LocalDateTime data;
}
