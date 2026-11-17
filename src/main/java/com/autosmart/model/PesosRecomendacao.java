package com.autosmart.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** Pesos ativos do ranking. Registro único: id = 1. */
@Entity
public class PesosRecomendacao {
    @Id public Long id = 1L;
    public int preco, categoria, cambio, ocupantes, consumo, finalidade, prioridades;

    public int total() {
        return preco + categoria + cambio + ocupantes + consumo + finalidade + prioridades;
    }
}
