package com.autosmart.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class Auditoria {
    @Id
    @GeneratedValue
    public Long id;
    public String usuario, acao;
    public LocalDateTime data;
}
