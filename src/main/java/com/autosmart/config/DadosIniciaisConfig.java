package com.autosmart.config;

import com.autosmart.model.Cidades;
import com.autosmart.model.Usuario;
import com.autosmart.model.Veiculo;
import com.autosmart.repository.UsuarioRepo;
import com.autosmart.repository.VeiculoRepo;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Carga inicial para uso local, executada somente no modo de demonstração. */
@Configuration
public class DadosIniciaisConfig {
    @Bean @Order(1) @ConditionalOnProperty(name = "autosmart.demo.enabled", havingValue = "true") CommandLineRunner seed(UsuarioRepo us,
            VeiculoRepo vs, PasswordEncoder enc) {
        return x -> {
            if (us.count() == 0) {
                user(us, enc, "Administrador", "admin@autosmart.com", "admin123", Usuario.Perfil.ADMIN);
                user(us, enc, "Gerente", "gerente@autosmart.com", "gerente123", Usuario.Perfil.GERENTE);
                user(us, enc, "Vendedor", "vendedor@autosmart.com", "vendedor123", Usuario.Perfil.VENDEDOR);
            }
            if (vs.count() == 0) {
                car(vs, "Fiat", "Argo", "Drive 1.3", 2024, "Branco", 12000, "Hatch", "Manual", 5, 13.5, "68900");
                car(vs, "Chevrolet", "Onix", "LT Turbo", 2025, "Prata", 5000, "Hatch", "Automático", 5, 14.2, "92500");
                car(vs, "Volkswagen", "T-Cross", "Comfortline", 2024, "Cinza", 18000, "SUV", "Automático", 5, 12.4, "128000");
                car(vs, "Toyota", "Corolla", "XEi 2.0", 2023, "Preto", 30000, "Sedan", "Automático", 5, 11.8, "139000");
                car(vs, "Hyundai", "HB20", "Sense 1.0", 2023, "Vermelho", 22000, "Hatch", "Manual", 5, 14.8, "72000");
                car(vs, "Fiat", "Toro", "Freedom", 2024, "Azul", 15000, "Picape", "Automático", 5, 10.9, "165000");
            }
            int i = 0;
            for (Veiculo v : vs.findAll()) if (v.cidade == null) {
                v.cidade = Cidades.BAHIA.get(i++ % 6);
                vs.save(v);
            }
        };
    }
    static void user(UsuarioRepo r, PasswordEncoder e, String n, String m, String s, Usuario.Perfil p) {
        Usuario u = new Usuario();
        u.nome = n;
        u.email = m;
        u.senha = e.encode(s);
        u.perfil = p;
        r.save(u);
    }
    static void car(VeiculoRepo r, String ma, String mo, String ve, int an, String co, int km, String ca, String cb, int lu, double cs, String pr) {
        Veiculo v = new Veiculo();
        v.marca = ma;
        v.modelo = mo;
        v.versao = ve;
        v.ano = an;
        v.cor = co;
        v.km = km;
        v.categoria = ca;
        v.cambio = cb;
        v.lugares = lu;
        v.consumo = cs;
        v.preco = new BigDecimal(pr);
        v.cidade = Cidades.BAHIA.get((int) (r.count() % 6));
        v.localizacao = "Pátio " + v.cidade;
        v.finalidades = switch (ca) {
            case "Hatch" -> "Cidade,Família";
            case "SUV", "Sedan" -> "Cidade,Família,Viagens";
            default -> "Trabalho,Viagens";
        };
        v.caracteristicas = cs >= 13.0 ? "Econômico,Ar-condicionado" : "Ar-condicionado";
        r.save(v);
    }
}
