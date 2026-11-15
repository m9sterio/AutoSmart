package com.autosmart.model;

import jakarta.persistence.*;

@Entity
public class Cliente {
    @Id
    @GeneratedValue
    public Long id;
    public String cidade;
    public String nome;
    @Column(unique = true)
    public String cpf;
    public String telefone;
    public String email;
    public boolean ativo = true;
}
