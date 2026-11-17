package com.autosmart.service;

import com.autosmart.model.Auditoria;
import com.autosmart.model.Cliente;
import com.autosmart.model.ClienteHistorico;
import com.autosmart.model.Proposta;
import com.autosmart.model.PropostaVersao;
import com.autosmart.model.Veiculo;
import com.autosmart.model.VeiculoHistorico;
import com.autosmart.repository.AuditoriaRepo;
import com.autosmart.repository.ClienteHistoricoRepo;
import com.autosmart.repository.PropostaVersaoRepo;
import com.autosmart.repository.VeiculoHistoricoRepo;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class HistoricoService {
    private final ClienteHistoricoRepo clientes;
    private final VeiculoHistoricoRepo veiculos;
    private final PropostaVersaoRepo propostas;
    private final AuditoriaRepo auditoria;
    HistoricoService(ClienteHistoricoRepo c, VeiculoHistoricoRepo v, PropostaVersaoRepo p, AuditoriaRepo a) {
        clientes = c;
        veiculos = v;
        propostas = p;
        auditoria = a;
    }
    public void cliente(Cliente c, Principal p, String acao) {
        ClienteHistorico h = new ClienteHistorico();
        h.cliente = c;
        h.nome = c.nome;
        h.telefone = c.telefone;
        h.email = c.email;
        h.cidade = c.cidade;
        h.ativo = c.ativo;
        h.acao = acao;
        h.usuario = p.getName();
        h.data = LocalDateTime.now();
        clientes.save(h);
        audit(p, "Cliente #" + c.id + ": " + acao);
    }
    public void veiculo(Veiculo v, Principal p, String acao) {
        VeiculoHistorico h = new VeiculoHistorico();
        h.veiculo = v;
        h.acao = acao;
        h.usuario = p.getName();
        h.data = LocalDateTime.now();
        h.dados = v.descricao() + " | cor=" + v.cor + " | km=" + v.km + " | preço=" + v.preco +
        " | status=" + v.status + " | cidade=" + v.cidade + " | local=" + v.localizacao +
        " | características=" + v.caracteristicas;
        veiculos.save(h);
        audit(p, "Veículo #" + v.id + ": " + acao);
    }
    public void proposta(Proposta proposta, Principal p, String acao) {
        PropostaVersao h = new PropostaVersao();
        h.proposta = proposta;
        h.versao = propostas.countByPropostaId(proposta.id) + 1;
        h.status = proposta.status;
        h.valor = proposta.valor;
        h.desconto = proposta.desconto;
        h.precoBase = proposta.precoBase == null ? proposta.valor.add(proposta.desconto == null ? java.math.BigDecimal.ZERO : proposta.desconto) : proposta.precoBase;
        h.acessorios = proposta.acessorios;
        h.validade = proposta.validade;
        h.responsavel = proposta.responsavel;
        h.usuario = p.getName();
        h.acao = acao;
        h.data = LocalDateTime.now();
        propostas.save(h);
        audit(p, "Proposta #" + proposta.id + ": " + acao + " (v" + h.versao + ")");
    }
    public void audit(Principal p, String acao) {
        Auditoria a = new Auditoria();
        a.usuario = p.getName();
        a.acao = acao;
        a.data = LocalDateTime.now();
        auditoria.save(a);
    }
}
