package com.autosmart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class SegurancaWebTest {
  @Autowired MockMvc mvc;

  @Test void anonimousNaoVisualizaDadosInternos() throws Exception {
    mvc.perform(get("/clientes")).andExpect(status().is3xxRedirection());
    mvc.perform(get("/")).andExpect(status().isOk());
  }
  @Test void mecanicoNaoTemAcessoComercial() throws Exception {
    mvc.perform(get("/clientes").with(user("mecanico").roles("MECANICO"))).andExpect(status().isForbidden());
    mvc.perform(get("/propostas").with(user("mecanico").roles("MECANICO"))).andExpect(status().isForbidden());
    mvc.perform(get("/oficina").with(user("mecanico").roles("MECANICO"))).andExpect(status().isOk());
  }
  @Test void vendedorNaoCadastraVeiculoOuAprovaVendas() throws Exception {
    mvc.perform(post("/veiculos").with(user("vendedor").roles("VENDEDOR")).with(csrf()))
       .andExpect(status().isForbidden());
    mvc.perform(post("/propostas/1/aprovar").with(user("vendedor").roles("VENDEDOR")).with(csrf()))
       .andExpect(status().isForbidden());
    mvc.perform(get("/usuarios").with(user("vendedor").roles("VENDEDOR"))).andExpect(status().isForbidden());
  }
  @Test void somenteVendedorConfirmaPerfilERecomendacao() throws Exception {
    mvc.perform(post("/recomendacao").with(user("admin").roles("ADMIN")).with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(post("/recomendacao").with(user("gerente").roles("GERENTE")).with(csrf()))
        .andExpect(status().isForbidden());
  }
  @Test void somenteAdministradorAlteraPesosDaRecomendacao() throws Exception {
    mvc.perform(get("/configuracoes/recomendacao").with(user("vendedor").roles("VENDEDOR")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/configuracoes/recomendacao").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk());
  }
  @Test void administradorPodeGerenciarUsuarios() throws Exception {
    mvc.perform(get("/usuarios").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
  }
  @Test void csrfProtegeOperacoesDeEscrita() throws Exception {
    mvc.perform(post("/clientes").with(user("vendedor").roles("VENDEDOR"))
        .param("nome", "Pessoa Teste").param("cpf", "99999999999"))
        .andExpect(status().isForbidden());
  }
  @Test void mecanicoNaoEncerraOrdemDeServico() throws Exception {
    mvc.perform(post("/oficina/1/encerrar").with(user("mecanico").roles("MECANICO")).with(csrf()))
        .andExpect(status().isForbidden());
  }
}
