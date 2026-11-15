package com.autosmart.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
public class Veiculo {
    public enum Status {
        DISPONIVEL, RESERVADO, VENDIDO
    }
    @Id
    @GeneratedValue
    public Long id;
    public String marca, modelo, versao, cor, categoria, cambio, localizacao;
    @Column(length = 1000)
    public String fotoUrl;
    // URL HTTPS da foto real do veículo, autorizada para uso
    @Column(length = 1500)
    public String caracteristicas;
    public String finalidades;
    public Integer ano, km, lugares;
    public Double consumo;
    public String cidade;
    public BigDecimal preco;
    @Enumerated(EnumType.STRING)
    public Status status = Status.DISPONIVEL;
    public String descricao() {
        return marca + " " + modelo + " " + versao + " " + ano;
    }
}
