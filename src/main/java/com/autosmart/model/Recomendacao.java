package com.autosmart.model;

import java.util.List;

public record Recomendacao(Veiculo veiculo, int score, List<String> positivos, List<String> atencao, List<String> sem) {
    public Veiculo getVeiculo() {
        return veiculo;
    }
    public int getScore() {
        return score;
    }
    public List<String> getPositivos() {
        return positivos;
    }
    public List<String> getAtencao() {
        return atencao;
    }
    public List<String> getSem() {
        return sem;
    }
}
