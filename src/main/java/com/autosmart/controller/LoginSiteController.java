package com.autosmart.controller;

import com.autosmart.model.Veiculo;
import com.autosmart.repository.VeiculoRepo;

import java.time.*;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Transactional
public class LoginSiteController {
    private final VeiculoRepo veiculos;
    LoginSiteController(VeiculoRepo v) {
        veiculos = v;
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/")
    String vitrine(@RequestParam(required = false) String cidade, @RequestParam(required = false) String categoria, Model model) {
        var lista = veiculos.findByStatus(Veiculo.Status.DISPONIVEL).stream()
        .filter(v -> cidade == null || cidade.isBlank() || cidade.equals(v.cidade))
        .filter(v -> categoria == null || categoria.isBlank() || categoria.equalsIgnoreCase(v.categoria)).toList();
        model.addAttribute("lista", lista);
        model.addAttribute("cidade", cidade);
        model.addAttribute("categoria", categoria);
        return "home";
    }
}
