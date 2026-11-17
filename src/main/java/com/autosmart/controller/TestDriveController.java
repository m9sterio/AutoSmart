package com.autosmart.controller;

import com.autosmart.model.Veiculo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.TestDriveRepo;
import com.autosmart.repository.VeiculoRepo;
import com.autosmart.service.ResultadoOperacao;
import com.autosmart.service.TestDriveService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class TestDriveController {
    private final TestDriveRepo agendamentos;
    private final VeiculoRepo veiculos;
    private final ClienteRepo clientes;
    private final TestDriveService servico;

    public TestDriveController(TestDriveRepo agendamentos, VeiculoRepo veiculos,
                               ClienteRepo clientes, TestDriveService servico) {
        this.agendamentos = agendamentos;
        this.veiculos = veiculos;
        this.clientes = clientes;
        this.servico = servico;
    }


    @GetMapping("/testdrives")

    String tds(Model model, @RequestParam(required = false) Long clienteId,
               @RequestParam(required = false) Long veiculoId) {
        model.addAttribute("lista", agendamentos.findAll());
        model.addAttribute("clientes", clientes.findByAtivoTrue());
        model.addAttribute("veiculos", veiculos.findByStatus(Veiculo.Status.DISPONIVEL));
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("veiculoId", veiculoId);
        return "testdrives";
    }


    @PostMapping("/testdrives")

    String agendar(@RequestParam Long veiculoId, @RequestParam Long clienteId,
                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                   LocalDateTime inicio, Principal usuario, RedirectAttributes redirect) {
        ResultadoOperacao resultado = servico.agendar(veiculoId, clienteId, inicio, usuario);
        redirect.addFlashAttribute(resultado.sucesso() ? "msg" : "erro", resultado.mensagem());
        return "redirect:/testdrives";
    }
}
