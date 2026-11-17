package com.autosmart.controller;

import com.autosmart.model.Cliente;
import com.autosmart.repository.ClienteHistoricoRepo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.service.HistoricoService;

import java.security.Principal;
import java.time.*;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Transactional
public class ClienteEdicaoController {
    private final ClienteRepo cs;
    private final HistoricoService historico;
    private final ClienteHistoricoRepo versoes;
    ClienteEdicaoController(ClienteRepo c, HistoricoService h, ClienteHistoricoRepo v) {
        cs = c;
        historico = h;
        versoes = v;
    }
    // ----- editar cliente -----
    @GetMapping("/clientes/{id}/editar")
    String editarForm(@PathVariable Long id, Model m) {
        m.addAttribute("c", cs.findById(id).orElseThrow());
        return "cliente-editar";
    }

    @PostMapping("/clientes/{id}/editar")
    String editar(@PathVariable Long id, @RequestParam String nome, @RequestParam String telefone, @RequestParam String email,
            @RequestParam String cidade, Principal p) {
        Cliente c = cs.findById(id).orElseThrow();
        if (nome == null || nome.isBlank()) return "redirect:/clientes/" + id + "/editar";
        if (versoes.findByClienteIdOrderByDataDesc(id).isEmpty()) historico.cliente(c, p, "Estado anterior à primeira edição");
        c.nome = nome;
        c.telefone = telefone;
        c.email = email;
        c.cidade = cidade;
        historico.cliente(c, p, "Edição");
        return "redirect:/clientes";
    }
}
