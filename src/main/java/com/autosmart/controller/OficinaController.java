package com.autosmart.controller;

import com.autosmart.model.Alerta;
import com.autosmart.model.Auditoria;
import com.autosmart.model.OrdemServico;
import com.autosmart.repository.AlertaRepo;
import com.autosmart.repository.AuditoriaRepo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.OrdemRepo;
import com.autosmart.repository.VeiculoRepo;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.*;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Transactional
public class OficinaController {
    private final OrdemRepo oss;
    private final ClienteRepo cs;
    private final VeiculoRepo vs;
    private final AuditoriaRepo au;
    private final AlertaRepo als;
    OficinaController(OrdemRepo o, ClienteRepo c, VeiculoRepo v, AuditoriaRepo a, AlertaRepo al) {
        oss = o;
        cs = c;
        vs = v;
        au = a;
        als = al;
    }
    // ----- oficina (RF11) -----
    @GetMapping("/oficina")
    String oficina(Model m) {
        m.addAttribute("lista", oss.findAll());
        m.addAttribute("clientes", cs.findByAtivoTrue());
        m.addAttribute("veiculos", vs.findAll());
        return "oficina";
    }

    @PostMapping("/oficina")
    String abrirOs(@RequestParam Long clienteId, @RequestParam Long veiculoId, @RequestParam String descricao, Principal p, RedirectAttributes ra) {
        OrdemServico o = new OrdemServico();
        o.cliente = cs.findById(clienteId).orElseThrow();
        if (!o.cliente.ativo) {
            ra.addFlashAttribute("erro", "Cliente inativo não pode iniciar ordem de serviço.");
            return "redirect:/oficina";
        }
        o.veiculo = vs.findById(veiculoId).orElseThrow();
        o.descricao = descricao;
        o.abertura = LocalDate.now();
        o.responsavel = p.getName();
        o.valor = BigDecimal.ZERO;
        oss.save(o);
        audit(p, "OS #" + o.id + " aberta");
        ra.addFlashAttribute("msg", "Ordem de serviço aberta.");
        return "redirect:/oficina";
    }

    @PostMapping("/oficina/{id}/atualizar")
    String atualizarOs(@PathVariable Long id, @RequestParam String diagnostico, @RequestParam String atividades, @RequestParam String pecas,
            @RequestParam BigDecimal valor, Principal p) {
        OrdemServico o = oss.findById(id).orElseThrow();
        if (o.status != OrdemServico.Status.ENCERRADA && valor != null && valor.signum() >= 0
                && diagnostico != null && !diagnostico.isBlank()
                && atividades != null && !atividades.isBlank()) {
            o.diagnostico = diagnostico;
            o.atividades = atividades;
            o.pecas = pecas;
            o.valor = valor;
            o.status = OrdemServico.Status.EM_ANDAMENTO;
            audit(p, "OS #" + id + " atualizada");
        }
        return "redirect:/oficina";
    }

    @PostMapping("/oficina/{id}/encerrar")
    String encerrarOs(@PathVariable Long id, Principal p, RedirectAttributes ra) {
        OrdemServico o = oss.findById(id).orElseThrow();
        if (o.status != OrdemServico.Status.ENCERRADA) {
            if (o.diagnostico == null || o.diagnostico.isBlank()
                    || o.atividades == null || o.atividades.isBlank()
                    || o.valor == null || o.valor.signum() < 0) {
                ra.addFlashAttribute("erro", "Para encerrar a OS, registre diagnóstico, serviços e valor válido.");
                return "redirect:/oficina";
            }
            o.status = OrdemServico.Status.ENCERRADA;
            audit(p, "OS #" + id + " encerrada, valor " + o.valor);
            Alerta a = new Alerta();
            a.tipo = "PÓS-SERVIÇO";
            a.mensagem = "Contato pós-serviço com " + o.cliente.nome + " (OS #" + id + ")";
            a.data = LocalDate.now().plusDays(7);
            a.responsavel = p.getName();
            als.save(a);
        }
        return "redirect:/oficina";
    }
    private void audit(Principal p, String acao) {
        Auditoria a = new Auditoria();
        a.usuario = p.getName();
        a.acao = acao;
        a.data = LocalDateTime.now();
        au.save(a);
    }
}
