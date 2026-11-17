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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Transactional
public class ClienteController {
    private final ClienteRepo clientes;
    private final HistoricoService historico;
    private final ClienteHistoricoRepo historicoClientes;
    ClienteController(ClienteRepo c, HistoricoService h, ClienteHistoricoRepo hc) {
        clientes = c;
        historico = h;
        historicoClientes = hc;
    }

    @GetMapping("/clientes")
    String clientes(Model m) {
        m.addAttribute("lista", clientes.findAll());
        return "clientes";
    }

    @PostMapping("/clientes")
    String salvarCliente(@ModelAttribute Cliente c, Principal p, RedirectAttributes ra) {
        if (c.nome == null || c.nome.isBlank() || c.cpf == null || c.cpf.isBlank()) {
            ra.addFlashAttribute("erro", "Informe nome e CPF.");
            return "redirect:/clientes";
        }
        var existente = clientes.findByCpf(c.cpf);
        if (existente.isPresent()) {
            ra.addFlashAttribute("erro", "CPF já cadastrado: " + existente.get().nome + ".");
            ra.addFlashAttribute("clienteExistenteId", existente.get().id);
            return "redirect:/clientes";
        }
        c.id = null;
        c.ativo = true;
        clientes.saveAndFlush(c);
        historico.cliente(c, p, "Cadastro");
        ra.addFlashAttribute("msg", "Cliente salvo.");
        return "redirect:/clientes";
    }

    @PostMapping("/clientes/{id}/inativar")
    String inativar(@PathVariable Long id, Principal p) {
        Cliente c = clientes.findById(id).orElseThrow();
        if (c.ativo) {
            if (historicoClientes.findByClienteIdOrderByDataDesc(id).isEmpty())
                historico.cliente(c, p, "Estado anterior à inativação");
            c.ativo = false;
            historico.cliente(c, p, "Inativação");
        }
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/{id}/historico")
    String historicoCliente(@PathVariable Long id, Model m) {
        m.addAttribute("cliente", clientes.findById(id).orElseThrow());
        m.addAttribute("lista", historicoClientes.findByClienteIdOrderByDataDesc(id));
        return "cliente-historico";
    }
}
