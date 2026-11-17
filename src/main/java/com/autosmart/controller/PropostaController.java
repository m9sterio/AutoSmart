package com.autosmart.controller;

import com.autosmart.model.Proposta;
import com.autosmart.model.Veiculo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.PropostaRepo;
import com.autosmart.repository.PropostaVersaoRepo;
import com.autosmart.repository.VeiculoRepo;
import com.autosmart.service.PropostaService;
import com.autosmart.service.ResultadoOperacao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;

/** Páginas e formulários de propostas; as regras ficam no PropostaService. */
@Controller
public class PropostaController {
    private final PropostaRepo propostas;
    private final VeiculoRepo veiculos;
    private final ClienteRepo clientes;
    private final PropostaVersaoRepo versoes;
    private final PropostaService servico;

    public PropostaController(PropostaRepo propostas, VeiculoRepo veiculos, ClienteRepo clientes,
                              PropostaVersaoRepo versoes, PropostaService servico) {
        this.propostas = propostas;
        this.veiculos = veiculos;
        this.clientes = clientes;
        this.versoes = versoes;
        this.servico = servico;
    }


    @GetMapping("/propostas")

    String props(Model model, @RequestParam(required = false) Long clienteId,
                 @RequestParam(required = false) Long veiculoId) {
        model.addAttribute("lista", propostas.findAll());
        model.addAttribute("clientes", clientes.findByAtivoTrue());
        model.addAttribute("veiculos", veiculos.findByStatus(Veiculo.Status.DISPONIVEL));
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("veiculoId", veiculoId);
        return "propostas";
    }


    @PostMapping("/propostas")

    String criar(@RequestParam Long clienteId, @RequestParam Long veiculoId,
                 @RequestParam(defaultValue = "0") BigDecimal desconto,
                 @RequestParam(required = false) String acessorios,
                 @RequestParam LocalDate validade, Principal usuario, RedirectAttributes redirect) {
        return voltarParaLista(redirect,
                servico.criar(clienteId, veiculoId, desconto, acessorios, validade, usuario));
    }


    @GetMapping("/propostas/{id}/editar")

    String editarProposta(@PathVariable Long id, Principal usuario, Model model) {
        Proposta proposta = propostas.findById(id).orElseThrow();
        servico.verificarPermissaoEdicao(proposta, usuario);
        model.addAttribute("p", proposta);
        return "proposta-editar";
    }


    @PostMapping("/propostas/{id}/editar")

    String atualizarProposta(@PathVariable Long id,
                             @RequestParam(defaultValue = "0") BigDecimal desconto,
                             @RequestParam(required = false) String acessorios,
                             @RequestParam LocalDate validade, Principal usuario,
                             RedirectAttributes redirect) {
        return voltarParaLista(redirect, servico.editar(id, desconto, acessorios, validade, usuario));
    }


    @GetMapping("/propostas/{id}/historico")

    String historicoProposta(@PathVariable Long id, Model model) {
        model.addAttribute("proposta", propostas.findById(id).orElseThrow());
        model.addAttribute("lista", versoes.findByPropostaIdOrderByVersaoDesc(id));
        return "proposta-historico";
    }


    @PostMapping("/propostas/{id}/aprovar")

    String aprovar(@PathVariable Long id, @RequestParam LocalDate entrega,
                   Principal usuario, RedirectAttributes redirect) {
        return voltarParaLista(redirect, servico.aprovar(id, entrega, usuario));
    }


    @PostMapping("/propostas/{id}/recusar")

    String recusar(@PathVariable Long id, Principal usuario, RedirectAttributes redirect) {
        return voltarParaLista(redirect, servico.recusar(id, usuario));
    }

    private String voltarParaLista(RedirectAttributes redirect, ResultadoOperacao resultado) {
        if (resultado.mensagem() != null) {
            redirect.addFlashAttribute(resultado.sucesso() ? "msg" : "erro", resultado.mensagem());
        }
        return "redirect:/propostas";
    }
}
