package com.autosmart.service;

import com.autosmart.model.PesosRecomendacao;
import com.autosmart.repository.PesosRecomendacaoRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Pesos persistidos, editáveis apenas na tela do Administrador. */
@Service
public class PesosRecomendacaoService {
    private final PesosRecomendacaoRepo repositorio;
    @Value("${autosmart.recomendacao.peso-preco:30}") int preco;
    @Value("${autosmart.recomendacao.peso-categoria:15}") int categoria;
    @Value("${autosmart.recomendacao.peso-cambio:15}") int cambio;
    @Value("${autosmart.recomendacao.peso-ocupantes:15}") int ocupantes;
    @Value("${autosmart.recomendacao.peso-consumo:10}") int consumo;
    @Value("${autosmart.recomendacao.peso-finalidade:10}") int finalidade;
    @Value("${autosmart.recomendacao.peso-prioridades:5}") int prioridades;

    public PesosRecomendacaoService(PesosRecomendacaoRepo repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public PesosRecomendacao atuais() {
        return repositorio.findById(1L).orElseGet(() -> {
            PesosRecomendacao p = new PesosRecomendacao();
            p.preco = preco; p.categoria = categoria; p.cambio = cambio;
            p.ocupantes = ocupantes; p.consumo = consumo;
            p.finalidade = finalidade; p.prioridades = prioridades;
            return p;
        });
    }

    @Transactional
    public void atualizar(PesosRecomendacao novos) {
        validar(novos);
        novos.id = 1L;
        repositorio.save(novos);
    }

    public void validar(PesosRecomendacao pesos) {
        int[] valores = {pesos.preco, pesos.categoria, pesos.cambio,
            pesos.ocupantes, pesos.consumo, pesos.finalidade, pesos.prioridades};
        for (int valor : valores) {
            if (valor < 0 || valor > 100) throw new IllegalArgumentException("Cada peso deve estar entre 0 e 100.");
        }
        if (pesos.total() != 100) throw new IllegalArgumentException("A soma dos pesos deve ser 100.");
    }
}
