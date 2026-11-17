package com.autosmart.controller;

import com.autosmart.model.Alerta;
import com.autosmart.model.Auditoria;
import com.autosmart.repository.AlertaRepo;
import com.autosmart.repository.AuditoriaRepo;

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
public class AlertasController {
    private final AlertaRepo als;
    private final AuditoriaRepo au;
    AlertasController(AlertaRepo a, AuditoriaRepo aud) {
        als = a;
        au = aud;
    }
    // ----- alertas (RF12 / regra 9) -----
    @GetMapping("/alertas")
    String alertas(Model m, Principal p) {
        var lista = als.findByConcluidoFalseOrderByData();
        m.addAttribute("lista", gerenteOuAdmin() ? lista : lista.stream().filter(a -> p.getName().equals(a.responsavel)).toList());
        return "alertas";
    }

    @PostMapping("/alertas/{id}/concluir")
    String concluir(@PathVariable Long id, Principal p) {
        Alerta a = als.findById(id).orElseThrow();
        if (!gerenteOuAdmin() && !p.getName().equals(a.responsavel))
            throw new AccessDeniedException("Somente responsável ou gestor pode concluir alerta.");
        a.concluido = true;
        audit(p, "Alerta #" + id + " concluído");
        return "redirect:/alertas";
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
