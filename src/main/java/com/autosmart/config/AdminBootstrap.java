package com.autosmart.config;

import com.autosmart.model.Usuario;
import com.autosmart.repository.UsuarioRepo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Inicialização segura para ambientes sem dados demonstrativos. Não sobrescreve contas existentes. */
@Configuration
public class AdminBootstrap {
    @Bean @Order(0) CommandLineRunner criarAdministradorSeConfigurado(UsuarioRepo usuarios, PasswordEncoder encoder, Environment env) {
        return args -> {
            String email = env.getProperty("autosmart.admin.email", "").trim();
            String senha = env.getProperty("autosmart.admin.password", "");
            if (email.isBlank() && senha.isBlank()) return;
            if (email.isBlank() || senha.length() < 12)
                throw new IllegalStateException("Configure AUTOSMART_ADMIN_EMAIL e senha com pelo menos 12 caracteres.");
            if (usuarios.findByEmail(email).isEmpty()) {
                Usuario u = new Usuario();
                u.nome = "Administrador";
                u.email = email;
                u.senha = encoder.encode(senha);
                u.perfil = Usuario.Perfil.ADMIN;
                usuarios.save(u);
            }
        };
    }
}
