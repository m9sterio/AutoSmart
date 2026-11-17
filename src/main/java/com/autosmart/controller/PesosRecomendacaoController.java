package com.autosmart.controller;

import com.autosmart.model.PesosRecomendacao;
import com.autosmart.service.HistoricoService;
import com.autosmart.service.PesosRecomendacaoService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Tela administrativa dos pesos; protegida por SecurityConfig. */
@Controller
@RequestMapping("/configuracoes/recomendacao")
public class PesosRecomendacaoController {
    private final PesosRecomendacaoService service;
    private final HistoricoService historico;
    public PesosRecomendacaoController(PesosRecomendacaoService service, HistoricoService historico) {
        this.service = service;
        this.historico = historico;
    }
    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("pesos", service.atuais());
        return "pesos-recomendacao";
    }
    @PostMapping
    public String salvar(@ModelAttribute PesosRecomendacao pesos, Principal usuario, RedirectAttributes redirect) {
        try {
            service.atualizar(pesos);
            historico.audit(usuario, "Pesos do motor de recomendação atualizados (soma=100)");
            redirect.addFlashAttribute("msg", "Pesos atualizados para as próximas recomendações.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/configuracoes/recomendacao";
    }
}
