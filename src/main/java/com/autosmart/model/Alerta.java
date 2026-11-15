package com.autosmart.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
    "origem_tipo", "origem_id"
}))
public class Alerta {
    @Id
    @GeneratedValue
    public Long id; public String tipo, mensagem,
            responsavel; public String origemTipo; public Long origemId; public LocalDate data; public boolean concluido;
}
