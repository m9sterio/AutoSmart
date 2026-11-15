package com.autosmart.model;

import jakarta.persistence.*;

@Entity
public class Usuario {
    public enum Perfil {
        ADMIN, GERENTE, VENDEDOR, CONSULTOR, MECANICO
    }
    @Id
    @GeneratedValue
    public Long id;
    public String nome;
    @Column(unique = true)
    public String email;
    public String senha;
    @Enumerated(EnumType.STRING)
    public Perfil perfil;
    public boolean ativo = true;
}
