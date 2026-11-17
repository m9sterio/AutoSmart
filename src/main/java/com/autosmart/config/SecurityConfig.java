package com.autosmart.config;

import com.autosmart.repository.UsuarioRepo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Acesso por módulo, inclusive GET, POST e URLs com identificador. CSRF permanece ativado. */
@Configuration
public class SecurityConfig {
    @Bean SecurityFilterChain chain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth .requestMatchers("/", "/login", "/css/**", "/img/**", "/error", "/actuator/health",
                "/actuator/health/**").permitAll() .requestMatchers("/configuracoes/recomendacao", "/configuracoes/recomendacao/**").hasRole("ADMIN") .requestMatchers("/usuarios",
                "/usuarios/**").hasRole("ADMIN") .requestMatchers(HttpMethod.POST, "/veiculos", "/veiculos/*/editar").hasAnyRole("ADMIN",
                "GERENTE") .requestMatchers(HttpMethod.GET, "/veiculos/*/editar").hasAnyRole("ADMIN",
                "GERENTE") .requestMatchers("/veiculos", "/veiculos/**").hasAnyRole("ADMIN", "GERENTE",
                "VENDEDOR") .requestMatchers(HttpMethod.POST, "/propostas/*/aprovar", "/propostas/*/recusar") .hasAnyRole("ADMIN",
                "GERENTE") .requestMatchers(org.springframework.http.HttpMethod.POST, "/recomendacao").hasRole("VENDEDOR") .requestMatchers("/clientes", "/clientes/**", "/recomendacao", "/recomendacao/**", "/testdrives",
                "/testdrives/**", "/propostas", "/propostas/**", "/oportunidades", "/oportunidades/**") .hasAnyRole("ADMIN", "GERENTE",
                "VENDEDOR") .requestMatchers(HttpMethod.POST, "/oficina", "/oficina/*/encerrar") .hasAnyRole("ADMIN", "GERENTE",
                "CONSULTOR") .requestMatchers("/oficina", "/oficina/**") .hasAnyRole("ADMIN", "GERENTE", "CONSULTOR",
                "MECANICO") .requestMatchers("/alertas", "/alertas/**") .hasAnyRole("ADMIN", "GERENTE", "VENDEDOR",
                "CONSULTOR") .requestMatchers("/painel").hasAnyRole("ADMIN", "GERENTE", "VENDEDOR", "CONSULTOR",
                "MECANICO") .anyRequest().denyAll())
        .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/painel", true).permitAll())
        .logout(logout -> logout.logoutSuccessUrl("/login?logout"));
        return http.build();
    }

    @Bean PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean UserDetailsService users(UsuarioRepo repo) {
        return email -> repo.findByEmail(email).filter(u -> u.ativo && u.perfil != null)
        .map(u -> User.withUsername(u.email).password(u.senha).roles(u.perfil.name()).build())
        .orElseThrow(() -> new UsernameNotFoundException(email));
    }
}
