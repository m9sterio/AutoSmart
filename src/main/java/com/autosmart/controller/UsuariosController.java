package com.autosmart.controller;

import com.autosmart.model.Auditoria;
import com.autosmart.model.Usuario;
import com.autosmart.repository.AuditoriaRepo;
import com.autosmart.repository.UsuarioRepo;

import java.security.Principal;
import java.time.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Transactional
public class UsuariosController {
    private final UsuarioRepo us;
    private final AuditoriaRepo au;
    private final PasswordEncoder enc;
    UsuariosController(UsuarioRepo u, AuditoriaRepo a, PasswordEncoder e) {
        us = u;
        au = a;
        enc = e;
    }
    // ----- usuários (somente ADMIN) -----
    @GetMapping("/usuarios")
    String usuarios(Model m) {
        m.addAttribute("lista", us.findAll());
        m.addAttribute("perfis", Usuario.Perfil.values());
        return "usuarios";
    }

    @PostMapping("/usuarios")
    String novoUsuario(@RequestParam String nome, @RequestParam String email, @RequestParam String senha,
            @RequestParam Usuario.Perfil perfil, Principal p, RedirectAttributes ra) {
        if (senha == null || senha.length() < 12 || email == null || !email.contains("@")) {
            ra.addFlashAttribute("erro", "Use e-mail válido e senha com pelo menos 12 caracteres.");
            return "redirect:/usuarios";
        }
        if (us.findByEmail(email).isPresent()) {
            ra.addFlashAttribute("erro", "E-mail já cadastrado.");
            return "redirect:/usuarios";
        }
        Usuario u = new Usuario();
        u.nome = nome;
        u.email = email;
        u.senha = enc.encode(senha);
        u.perfil = perfil;
        us.save(u);
        audit(p, "Usuário criado: " + email);
        ra.addFlashAttribute("msg", "Usuário criado.");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/{id}/alternar")
    String alternar(@PathVariable Long id, Principal p) {
        Usuario u = us.findById(id).orElseThrow();
        if (!u.email.equals(p.getName())) {
            u.ativo = !u.ativo;
            audit(p, "Usuário " + u.email + (u.ativo ? " ativado" : " inativado"));
        }
        return "redirect:/usuarios";
    }
    private void audit(Principal p, String acao) {
        Auditoria a = new Auditoria();
        a.usuario = p.getName();
        a.acao = acao;
        a.data = LocalDateTime.now();
        au.save(a);
    }
}
