package com.autosmart.controller;

import com.autosmart.model.Cliente;
import com.autosmart.model.PerfilCompra;
import com.autosmart.model.Recomendacao;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.PerfilRepo;
import com.autosmart.repository.RecomendacaoHistoricoRepo;
import com.autosmart.service.HistoricoService;
import com.autosmart.service.RecomendacaoService;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.*;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Transactional
public class RecomendacaoController {
    private final PerfilRepo perfis;
    private final ClienteRepo clientes;
    private final RecomendacaoService rec;
    private final HistoricoService historico;
    private final RecomendacaoHistoricoRepo recomendacoes;
    RecomendacaoController(PerfilRepo p, ClienteRepo c, RecomendacaoService r, HistoricoService h, RecomendacaoHistoricoRepo rh) {
        perfis = p;
        clientes = c;
        rec = r;
        historico = h;
        recomendacoes = rh;
    }

    @GetMapping("/recomendacao")
    String recForm(@RequestParam(required = false) Long perfilId, Model m) {
        m.addAttribute("clientes", clientes.findByAtivoTrue());
        m.addAttribute("perfis", perfis.findAll());
        if (perfilId != null) m.addAttribute("perfil", perfis.findById(perfilId).orElseThrow());
        return "recomendacao";
    }

    @PostMapping("/recomendacao")
    String recomendar( @RequestParam(required = false) Long perfilId, @RequestParam Long clienteId, @RequestParam BigDecimal orcamento,
            @RequestParam(required = false) String finalidade, @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String cambio, @RequestParam(required = false) Integer ocupantes,
            @RequestParam(required = false) Double consumoMin, @RequestParam(required = false) String prioridades, Principal p, Model m,
            RedirectAttributes ra) {
        if (orcamento == null || orcamento.signum() <= 0 || (ocupantes != null && ocupantes < 1) || (consumoMin != null && consumoMin < 0)) {
            ra.addFlashAttribute("erro", "Orçamento, ocupantes ou consumo inválidos.");
            return "redirect:/recomendacao";
        }
        Cliente cliente = clientes.findById(clienteId).orElseThrow();
        if (!cliente.ativo) {
            ra.addFlashAttribute("erro", "Cliente inativo.");
            return "redirect:/recomendacao";
        }
        PerfilCompra perfil = perfilId == null ? new PerfilCompra() : perfis.findById(perfilId).orElseThrow();
        if (perfilId != null && !perfil.cliente.id.equals(clienteId))
            throw new IllegalArgumentException("Não é possível transferir perfil para outro cliente.");
        perfil.cliente = cliente;
        perfil.orcamento = orcamento;
        perfil.finalidade = finalidade;
        perfil.categoria = categoria;
        perfil.cambio = cambio;
        perfil.ocupantes = ocupantes;
        perfil.consumoMin = consumoMin;
        perfil.prioridades = prioridades;
        perfis.saveAndFlush(perfil);
        List<Recomendacao> resultado = rec.recomendar(perfil);
        rec.registrar(perfil, resultado, p.getName());
        historico.audit(p, "Perfil #" + perfil.id + " revisado e recomendação gerada");
        m.addAttribute("clientes", clientes.findByAtivoTrue());
        m.addAttribute("perfis", perfis.findAll());
        m.addAttribute("perfil", perfil);
        m.addAttribute("resultado", resultado);
        return "recomendacao";
    }

    @GetMapping("/recomendacao/historico/{perfilId}")
    String historicoRecomendacao(@PathVariable Long perfilId, Model m) {
        m.addAttribute("perfil", perfis.findById(perfilId).orElseThrow());
        m.addAttribute("lista", recomendacoes.findByPerfilIdOrderByDataDescClassificacaoAsc(perfilId));
        return "recomendacao-historico";
    }
}
