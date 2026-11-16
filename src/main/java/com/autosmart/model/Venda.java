package com.autosmart.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class Venda {
    @Id
    @GeneratedValue
    public Long id;
    @OneToOne public Proposta proposta;
    public LocalDate data, entrega;
}
