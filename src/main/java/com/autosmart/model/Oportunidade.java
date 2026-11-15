package com.autosmart.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class Oportunidade {
    public enum Etapa {
        NOVO, CONTATO, TEST_DRIVE, PROPOSTA, FECHADO, PERDIDO
    }
    @Id
    @GeneratedValue
    public Long id;
    @ManyToOne
    public Cliente cliente;
    public String origem, observacoes, responsavel;
    public LocalDate proximaAcao;
    @Enumerated(EnumType.STRING)
    public Etapa etapa = Etapa.NOVO;
}
