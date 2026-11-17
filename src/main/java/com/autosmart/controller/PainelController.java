package com.autosmart.controller;

import com.autosmart.model.Oportunidade;
import com.autosmart.model.OrdemServico;
import com.autosmart.model.Proposta;
import com.autosmart.model.Veiculo;
import com.autosmart.repository.AlertaRepo;
import com.autosmart.repository.AuditoriaRepo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.OportunidadeRepo;
import com.autosmart.repository.OrdemRepo;
import com.autosmart.repository.PropostaRepo;
import com.autosmart.repository.VeiculoRepo;
import com.autosmart.repository.VendaRepo;

import java.time.*;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Transactional
public class PainelController {
    private final VeiculoRepo veiculos;
    private final PropostaRepo propostas;
    private final VendaRepo vendas;
    private final ClienteRepo clientes;
    private final OportunidadeRepo oportunidades;
    private final OrdemRepo ordens;
    private final AlertaRepo alertas;
    private final AuditoriaRepo auditoria;
    PainelController(VeiculoRepo v, PropostaRepo p, VendaRepo vd, ClienteRepo c, OportunidadeRepo o, OrdemRepo os, AlertaRepo a, AuditoriaRepo au) {
        veiculos = v;
        propostas = p;
        vendas = vd;
        clientes = c;
        oportunidades = o;
        ordens = os;
        alertas = a;
        auditoria = au;
    }

    @GetMapping("/painel")
    String painel(Model m) {
        m.addAttribute("disponiveis", veiculos.countByStatus(Veiculo.Status.DISPONIVEL));
        m.addAttribute("reservados", veiculos.countByStatus(Veiculo.Status.RESERVADO));
        m.addAttribute("abertas", propostas.countByStatus(Proposta.Status.ABERTA));
        m.addAttribute("vendas", vendas.count());
        m.addAttribute("clientes", clientes.findByAtivoTrue().size());
        m.addAttribute("oportunidadesAtivas",
                oportunidades.countByEtapa(Oportunidade.Etapa.NOVO) + oportunidades.countByEtapa(Oportunidade.Etapa.CONTATO) + oportunidades.countByEtapa(Oportunidade.Etapa.TEST_DRIVE) + oportunidades.countByEtapa(Oportunidade.Etapa.PROPOSTA));
        m.addAttribute("oportunidadesFechadas", oportunidades.countByEtapa(Oportunidade.Etapa.FECHADO));
        m.addAttribute("osAbertas", ordens.countByStatus(OrdemServico.Status.ABERTA) + ordens.countByStatus(OrdemServico.Status.EM_ANDAMENTO));
        m.addAttribute("osEncerradas", ordens.countByStatus(OrdemServico.Status.ENCERRADA));
        m.addAttribute("alertasPendentes", alertas.countByConcluidoFalse());
        m.addAttribute("log", auditoria.findTop20ByOrderByDataDesc());
        return "painel";
    }
}
