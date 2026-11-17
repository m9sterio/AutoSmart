package com.autosmart.service;

import com.autosmart.model.PerfilCompra;
import com.autosmart.model.Recomendacao;
import com.autosmart.model.RecomendacaoHistorico;
import com.autosmart.model.Veiculo;
import com.autosmart.repository.RecomendacaoHistoricoRepo;
import com.autosmart.repository.VeiculoRepo;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Service;

/** Regras explicáveis, pesos configuráveis e persistência do resultado apresentado. */
@Service
public class RecomendacaoService {
    private final VeiculoRepo veiculos;
    private final RecomendacaoHistoricoRepo historicos;
    private final PesosRecomendacaoService configuracoes;
    RecomendacaoService(VeiculoRepo v, RecomendacaoHistoricoRepo h, PesosRecomendacaoService configuracoes) {
        veiculos = v;
        historicos = h;
        this.configuracoes = configuracoes;
    }
    public List<Recomendacao> recomendar(PerfilCompra p) {
        if (p == null || p.orcamento == null || p.orcamento.signum() <= 0)
            throw new IllegalArgumentException("Informe orçamento positivo.");
        // Usa uma fotografia única dos pesos por operação; histórico conserva o score calculado.
        var pesos = configuracoes.atuais();
        configuracoes.validar(pesos);
        int pesoPreco = pesos.preco, pesoCategoria = pesos.categoria, pesoCambio = pesos.cambio;
        int pesoOcupantes = pesos.ocupantes, pesoConsumo = pesos.consumo;
        int pesoFinalidade = pesos.finalidade, pesoPrioridades = pesos.prioridades;
        List<Recomendacao> ranking = new ArrayList<>();
        for (Veiculo v : veiculos.findByStatus(Veiculo.Status.DISPONIVEL)) {
            if (v.preco == null || v.preco.signum() < 0) continue;
            int pontos = 0;
            List<String> positivos = new ArrayList<>(), atencao = new ArrayList<>(), sem = new ArrayList<>();
            if (v.preco.compareTo(p.orcamento) <= 0) {
                pontos += pesoPreco;
                positivos.add("Preço R$ " + v.preco + " dentro do orçamento");
            }
            else if (v.preco.compareTo(p.orcamento.multiply(new BigDecimal("1.10"))) <= 0) {
                pontos += pesoPreco / 3;
                atencao.add("Preço até 10% acima do orçamento");
            }
            else sem.add("Preço acima do orçamento");
            if (eq(v.categoria, p.categoria)) {
                pontos += pesoCategoria;
                positivos.add("Categoria " + v.categoria);
            }
            else sem.add("Categoria diferente da desejada: " + p.categoria);
            if (eq(v.cambio, p.cambio)) {
                pontos += pesoCambio;
                positivos.add("Câmbio " + v.cambio);
            }
            else atencao.add("Câmbio " + v.cambio + " (desejado: " + p.cambio + ")");
            if (p.ocupantes == null || (v.lugares != null && v.lugares >= p.ocupantes)) {
                pontos += pesoOcupantes;
                positivos.add("Quantidade de lugares adequada");
            }
            else sem.add("Lugares insuficientes");
            if (p.consumoMin == null || (v.consumo != null && v.consumo >= p.consumoMin)) {
                pontos += pesoConsumo;
                positivos.add("Consumo atende à preferência");
            }
            else atencao.add("Consumo " + v.consumo + " km/l abaixo do desejado");
            if (p.finalidade != null && !p.finalidade.isBlank()) {
                if (contem(v.finalidades, p.finalidade)) {
                    pontos += pesoFinalidade;
                    positivos.add("Adequado à finalidade: " + p.finalidade);
                }
                else if (v.finalidades == null || v.finalidades.isBlank()) atencao.add("Finalidade não informada para o veículo");
                else sem.add("Não indicado para finalidade: " + p.finalidade);
            }
            if (p.prioridades != null && !p.prioridades.isBlank()) {
                String[] criterios = p.prioridades.split("[,;]");
                int atendidos = 0, total = 0;
                for (String bruto : criterios) {
                    String criterio = bruto.trim();
                    if (criterio.isBlank()) continue;
                    total++;
                    boolean combina = contem(v.caracteristicas, criterio) ||
                    (normalizar(criterio).contains("econom") && v.consumo != null && v.consumo >= 13.0);
                    if (combina) {
                        atendidos++;
                        positivos.add("Prioridade atendida: " + criterio);
                    }
                    else sem.add("Prioridade sem correspondência: " + criterio);
                }
                if (total > 0) pontos += Math.round((float) pesoPrioridades * atendidos / total);
            }
            ranking.add(new Recomendacao(v, pontos, List.copyOf(positivos), List.copyOf(atencao), List.copyOf(sem)));
        }
        ranking.sort(Comparator.comparingInt(Recomendacao::score).reversed() .thenComparing(r -> r.veiculo().id));
        return ranking;
    }
    /** Guarda a pontuação e as justificativas exatas exibidas no instante da consulta. */
    public void registrar(PerfilCompra perfil, List<Recomendacao> ranking, String usuario) {
        String resumo = "orçamento=" + perfil.orcamento + "; finalidade=" + perfil.finalidade +
        "; categoria=" + perfil.categoria + "; câmbio=" + perfil.cambio +
        "; ocupantes=" + perfil.ocupantes + "; consumo=" + perfil.consumoMin +
        "; prioridades=" + perfil.prioridades;
        int posicao = 1;
        LocalDateTime agora = LocalDateTime.now();
        for (Recomendacao r : ranking) {
            RecomendacaoHistorico h = new RecomendacaoHistorico();
            h.perfil = perfil;
            h.veiculo = r.veiculo();
            h.classificacao = posicao++;
            h.pontuacao = r.score();
            h.usuario = usuario;
            h.veiculoDescricao = r.veiculo().descricao();
            h.perfilResumo = resumo;
            h.data = agora;
            h.positivos = String.join("\n", r.positivos());
            h.atencao = String.join("\n", r.atencao());
            h.semCorrespondencia = String.join("\n", r.sem());
            historicos.save(h);
        }
    }
    private static boolean eq(String a, String b) {
        return !normalizar(a).isBlank() && normalizar(a).equals(normalizar(b));
    }
    private static boolean contem(String lista, String procura) {
        if (lista == null || procura == null) return false;
        for (String x : lista.split("[,;]")) if (eq(x.trim(), procura.trim())) return true;
        return false;
    }
    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.strip().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "");
    }
}
