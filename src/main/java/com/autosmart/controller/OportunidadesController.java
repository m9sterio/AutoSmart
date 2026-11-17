package com.autosmart.controller;

import com.autosmart.model.Auditoria;
import com.autosmart.model.Oportunidade;
import com.autosmart.repository.AuditoriaRepo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.OportunidadeRepo;
import com.autosmart.service.AlertaService;

import java.security.Principal;
import java.time.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Transactional
public class OportunidadesController {
    private final OportunidadeRepo ops;
    private final ClienteRepo cs;
    private final AuditoriaRepo au;
    private final AlertaService alertaService;
    OportunidadesController(OportunidadeRepo o, ClienteRepo c, AuditoriaRepo a, AlertaService al) {
        ops = o;
        cs = c;
        au = a;
        alertaService = al;
    }
    // ----- oportunidades (RF07) -----
    @GetMapping("/oportunidades")
    String oportunidades(Model m) {
        m.addAttribute("lista", ops.findAll());
        m.addAttribute("clientes", cs.findByAtivoTrue());
        m.addAttribute("etapas", Oportunidade.Etapa.values());
        return "oportunidades";
    }

    @PostMapping("/oportunidades")
    String novaOp(@RequestParam Long clienteId, @RequestParam String origem, @RequestParam(required = false) String observacoes,
            @RequestParam LocalDate proximaAcao, Principal p) {
        Oportunidade o = new Oportunidade();
        o.cliente = cs.findById(clienteId).orElseThrow();
        if (!o.cliente.ativo) {
            throw new IllegalArgumentException("Cliente inativo não pode receber nova oportunidade.");
        }
        o.origem = origem;
        o.observacoes = observacoes;
        o.proximaAcao = proximaAcao;
        o.responsavel = p.getName();
        ops.saveAndFlush(o);
        alertaService.oportunidade(o);
        audit(p, "Oportunidade criada: " + o.cliente.nome);
        return "redirect:/oportunidades";
    }

    @PostMapping("/oportunidades/{id}/etapa")
    String etapa(@PathVariable Long id, @RequestParam Oportunidade.Etapa etapa, @RequestParam(required = false) LocalDate proximaAcao, Principal p) {
        Oportunidade o = ops.findById(id).orElseThrow();
        if (!gerenteOuAdmin() && !p.getName().equals(o.responsavel))
            throw new AccessDeniedException("Somente responsável ou gestor pode atualizar oportunidade.");
        o.etapa = etapa;
        if (proximaAcao != null) o.proximaAcao = proximaAcao;
        alertaService.oportunidade(o);
        audit(p, "Oportunidade #" + id + " -> " + etapa);
        return "redirect:/oportunidades";
    }
    private void audit(Principal p, String acao) {
        Auditoria a = new Auditoria();
        a.usuario = p.getName();
        a.acao = acao;
        a.data = LocalDateTime.now();
        au.save(a);
    }
    private boolean gerenteOuAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_GERENTE".equals(a.getAuthority()));
    }
}
