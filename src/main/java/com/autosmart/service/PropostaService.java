package com.autosmart.service;

import com.autosmart.model.Alerta;
import com.autosmart.model.Cliente;
import com.autosmart.model.Proposta;
import com.autosmart.model.Veiculo;
import com.autosmart.model.Venda;
import com.autosmart.repository.AlertaRepo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.PropostaRepo;
import com.autosmart.repository.PropostaVersaoRepo;
import com.autosmart.repository.VeiculoRepo;
import com.autosmart.repository.VendaRepo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;

/** Regras das propostas e das vendas. As telas ficam no PropostaController. */
@Service
@Transactional
public class PropostaService {
    private final PropostaRepo propostas;
    private final VeiculoRepo veiculos;
    private final ClienteRepo clientes;
    private final VendaRepo vendas;
    private final AlertaRepo alertas;
    private final PropostaVersaoRepo versoes;
    private final HistoricoService historico;

    public PropostaService(PropostaRepo propostas, VeiculoRepo veiculos, ClienteRepo clientes,
                           VendaRepo vendas, AlertaRepo alertas, PropostaVersaoRepo versoes,
                           HistoricoService historico) {
        this.propostas = propostas;
        this.veiculos = veiculos;
        this.clientes = clientes;
        this.vendas = vendas;
        this.alertas = alertas;
        this.versoes = versoes;
        this.historico = historico;
    }

    public ResultadoOperacao criar(Long clienteId, Long veiculoId, BigDecimal desconto,
                                  String acessorios, LocalDate validade, Principal usuario) {
        Veiculo veiculo = veiculos.bloquear(veiculoId).orElseThrow();
        Cliente cliente = clientes.findById(clienteId).orElseThrow();

        if (veiculo.status != Veiculo.Status.DISPONIVEL || !cliente.ativo) {
            return ResultadoOperacao.erro("Veículo indisponível ou cliente inativo.");
        }
        if (!descontoValido(desconto, veiculo.preco) || validade.isBefore(LocalDate.now())) {
            return ResultadoOperacao.erro("Desconto ou validade inválidos.");
        }

        Proposta proposta = new Proposta();
        proposta.cliente = cliente;
        proposta.veiculo = veiculo;
        proposta.desconto = desconto;
        proposta.precoBase = veiculo.preco;
        proposta.valor = veiculo.preco.subtract(desconto);
        proposta.acessorios = acessorios;
        proposta.validade = validade;
        proposta.responsavel = usuario.getName();
        propostas.saveAndFlush(proposta);

        veiculo.status = Veiculo.Status.RESERVADO;
        historico.veiculo(veiculo, usuario, "Reservado pela proposta #" + proposta.id);
        historico.proposta(proposta, usuario, "Criação");
        return ResultadoOperacao.ok("Proposta criada; veículo reservado.");
    }

    public ResultadoOperacao editar(Long id, BigDecimal desconto, String acessorios,
                                   LocalDate validade, Principal usuario) {
        Proposta proposta = propostas.bloquear(id).orElseThrow();
        verificarPermissaoEdicao(proposta, usuario);

        // Propostas antigas podem não ter o preço base salvo separadamente.
        BigDecimal precoBase = proposta.precoBase == null
                ? proposta.valor.add(proposta.desconto == null ? BigDecimal.ZERO : proposta.desconto)
                : proposta.precoBase;

        if (proposta.status != Proposta.Status.ABERTA || !descontoValido(desconto, precoBase)
                || validade.isBefore(LocalDate.now())) {
            return ResultadoOperacao.erro("Proposta não pode ser editada com esses dados.");
        }

        if (versoes.countByPropostaId(id) == 0) {
            historico.proposta(proposta, usuario, "Versão anterior à edição");
        }
        proposta.precoBase = precoBase;
        proposta.desconto = desconto;
        proposta.acessorios = acessorios;
        proposta.validade = validade;
        proposta.valor = precoBase.subtract(desconto);
        historico.proposta(proposta, usuario, "Edição");
        return ResultadoOperacao.ok("Proposta atualizada com histórico de versões.");
    }

    public ResultadoOperacao aprovar(Long id, LocalDate entrega, Principal usuario) {
        Proposta proposta = propostas.bloquear(id).orElseThrow();
        Veiculo veiculo = veiculos.bloquear(proposta.veiculo.id).orElseThrow();

        if (proposta.status != Proposta.Status.ABERTA
                || veiculo.status != Veiculo.Status.RESERVADO
                || proposta.validade.isBefore(LocalDate.now())
                || entrega.isBefore(LocalDate.now())) {
            return ResultadoOperacao.erro("Proposta vencida, já decidida ou com entrega inválida.");
        }

        if (versoes.countByPropostaId(id) == 0) {
            historico.proposta(proposta, usuario, "Versão anterior à decisão");
        }
        proposta.status = Proposta.Status.APROVADA;
        veiculo.status = Veiculo.Status.VENDIDO;

        Venda venda = new Venda();
        venda.proposta = proposta;
        venda.data = LocalDate.now();
        venda.entrega = entrega;
        vendas.save(venda);

        historico.veiculo(veiculo, usuario, "Vendido pela proposta #" + id);
        historico.proposta(proposta, usuario, "Aprovação humana e venda");

        Alerta alerta = new Alerta();
        alerta.tipo = "PÓS-VENDA";
        alerta.mensagem = "Revisão de " + veiculo.descricao() + " (cliente " + proposta.cliente.nome + ")";
        alerta.data = entrega.plusMonths(6);
        alerta.responsavel = proposta.responsavel;
        alertas.save(alerta);
        return ResultadoOperacao.ok("Venda registrada.");
    }

    public ResultadoOperacao recusar(Long id, Principal usuario) {
        Proposta proposta = propostas.bloquear(id).orElseThrow();
        Veiculo veiculo = veiculos.bloquear(proposta.veiculo.id).orElseThrow();

        if (proposta.status != Proposta.Status.ABERTA) {
            return ResultadoOperacao.ok(null);
        }
        if (versoes.countByPropostaId(id) == 0) {
            historico.proposta(proposta, usuario, "Versão anterior à decisão");
        }

        proposta.status = Proposta.Status.RECUSADA;
        veiculo.status = Veiculo.Status.DISPONIVEL;
        historico.veiculo(veiculo, usuario, "Reserva liberada pela proposta #" + id);
        historico.proposta(proposta, usuario, "Recusa humana");
        return ResultadoOperacao.ok("Reserva liberada.");
    }

    public void verificarPermissaoEdicao(Proposta proposta, Principal usuario) {
        boolean gestor = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_GERENTE"));
        if (!gestor && !proposta.responsavel.equals(usuario.getName())) {
            throw new AccessDeniedException("Somente responsável ou gestor pode editar esta proposta.");
        }
    }

    private boolean descontoValido(BigDecimal desconto, BigDecimal preco) {
        return desconto != null && preco != null && desconto.signum() >= 0
                && desconto.compareTo(preco) <= 0;
    }
}
