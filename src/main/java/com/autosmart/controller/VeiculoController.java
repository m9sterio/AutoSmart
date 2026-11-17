package com.autosmart.controller;

import com.autosmart.model.Veiculo;
import com.autosmart.repository.VeiculoHistoricoRepo;
import com.autosmart.repository.VeiculoRepo;
import com.autosmart.service.HistoricoService;

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
public class VeiculoController {
    private final VeiculoRepo veiculos;
    private final HistoricoService historico;
    private final VeiculoHistoricoRepo historicoVeiculos;
    VeiculoController(VeiculoRepo v, HistoricoService h, VeiculoHistoricoRepo hv) {
        veiculos = v;
        historico = h;
        historicoVeiculos = hv;
    }

    @GetMapping("/veiculos")
    String veiculos(Model m) {
        m.addAttribute("lista", veiculos.findAll());
        return "veiculos";
    }

    @PostMapping("/veiculos")
    String salvarVeiculo(@ModelAttribute Veiculo v, Principal p, RedirectAttributes ra) {
        if (v.marca == null || v.marca.isBlank() || v.modelo == null || v.modelo.isBlank()
                || v.preco == null || v.preco.signum() <= 0 || v.ano == null || v.ano < 1900) {
            ra.addFlashAttribute("erro", "Preencha marca, modelo, ano e preço válido.");
            return "redirect:/veiculos";
        }
        v.id = null;
        v.status = Veiculo.Status.DISPONIVEL;
        v.fotoUrl = fotoSegura(v.fotoUrl);
        if (v.localizacao == null || v.localizacao.isBlank()) v.localizacao = "Pátio " + v.cidade;
        veiculos.saveAndFlush(v);
        historico.veiculo(v, p, "Cadastro");
        ra.addFlashAttribute("msg", "Veículo cadastrado.");
        return "redirect:/veiculos";
    }

    @GetMapping("/veiculos/{id}/editar")
    String editarVeiculo(@PathVariable Long id, Model m) {
        m.addAttribute("v", veiculos.findById(id).orElseThrow());
        return "veiculo-editar";
    }

    @PostMapping("/veiculos/{id}/editar")
    String atualizarVeiculo(@PathVariable Long id, @RequestParam String marca, @RequestParam String modelo, @RequestParam String versao,
            @RequestParam String cor, @RequestParam int ano, @RequestParam int km, @RequestParam String categoria,
            @RequestParam String cambio, @RequestParam int lugares, @RequestParam double consumo, @RequestParam BigDecimal preco,
            @RequestParam String cidade, @RequestParam String localizacao, @RequestParam(required = false) String fotoUrl,
            @RequestParam(required = false) String finalidades, @RequestParam(required = false) String caracteristicas, Principal p,
            RedirectAttributes ra) {
        Veiculo v = veiculos.bloquear(id).orElseThrow();
        if (preco.signum() <= 0 || ano < 1900 || km < 0 || lugares <= 0 || consumo < 0 || marca.isBlank() || modelo.isBlank()) {
            ra.addFlashAttribute("erro", "Dados inválidos.");
            return "redirect:/veiculos/" + id + "/editar";
        }
        if (historicoVeiculos.findByVeiculoIdOrderByDataDesc(id).isEmpty())
            historico.veiculo(v, p, "Estado anterior à primeira edição");
        v.marca = marca;
        v.modelo = modelo;
        v.versao = versao;
        v.cor = cor;
        v.ano = ano;
        v.km = km;
        v.categoria = categoria;
        v.cambio = cambio;
        v.lugares = lugares;
        v.consumo = consumo;
        v.preco = preco;
        v.cidade = cidade;
        v.localizacao = localizacao;
        v.finalidades = finalidades;
        v.caracteristicas = caracteristicas;
        v.fotoUrl = fotoSegura(fotoUrl);
        historico.veiculo(v, p, "Alteração de dados / preço");
        ra.addFlashAttribute("msg", "Veículo atualizado.");
        return "redirect:/veiculos";
    }
    private String fotoSegura(String url) {
        if (url == null || url.isBlank()) return null;
        String clean = url.trim();
        if (!clean.matches("(?i)^https://[^\\s<>\"']+$") || clean.length() > 1000)
            throw new IllegalArgumentException("Foto deve ser uma URL HTTPS válida.");
        return clean;
    }

    @GetMapping("/veiculos/{id}/historico")
    String historicoVeiculo(@PathVariable Long id, Model m) {
        m.addAttribute("veiculo", veiculos.findById(id).orElseThrow());
        m.addAttribute("lista", historicoVeiculos.findByVeiculoIdOrderByDataDesc(id));
        return "veiculo-historico";
    }
}
