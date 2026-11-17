package com.autosmart;

import com.autosmart.model.*;
import com.autosmart.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Exercita o CRUD que a interface realmente disponibiliza. Usa H2 e rollback. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CrudCompletoTest {
    @Autowired MockMvc mvc;
    @Autowired ClienteRepo clientes;
    @Autowired VeiculoRepo veiculos;
    @Autowired OportunidadeRepo oportunidades;
    @Autowired UsuarioRepo usuarios;
    @Autowired AlertaRepo alertas;
    @Autowired OrdemRepo ordens;
    @Autowired PerfilRepo perfis;

    private Cliente cliente(String cpf) {
        Cliente c = new Cliente();
        c.nome = "Cliente CRUD"; c.cpf = cpf; c.cidade = "Salvador";
        return clientes.saveAndFlush(c);
    }

    private Veiculo veiculo() {
        Veiculo v = new Veiculo();
        v.marca = "Fiat"; v.modelo = "Argo"; v.versao = "1.0";
        v.ano = 2023; v.preco = new BigDecimal("55000");
        v.km = 10000; v.lugares = 5; v.consumo = 12.0;
        return veiculos.saveAndFlush(v);
    }

    @Test void clienteCriaListaEditaEInativaSemExcluirHistorico() throws Exception {
        mvc.perform(post("/clientes").with(user("vendedor").roles("VENDEDOR")).with(csrf())
                .param("nome", "Cliente Novo").param("cpf", "99911122200"))
                .andExpect(status().is3xxRedirection());
        Cliente c = clientes.findByCpf("99911122200").orElseThrow();
        mvc.perform(get("/clientes").with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isOk());
        mvc.perform(post("/clientes/" + c.id + "/editar").with(user("vendedor").roles("VENDEDOR")).with(csrf())
                .param("nome", "Nome Alterado").param("telefone", "71999999999")
                .param("email", "crud@example.com").param("cidade", "Camaçari"))
                .andExpect(status().is3xxRedirection());
        assertEquals("Nome Alterado", clientes.findById(c.id).orElseThrow().nome);
        mvc.perform(post("/clientes/" + c.id + "/inativar").with(user("vendedor").roles("VENDEDOR")).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertFalse(clientes.findById(c.id).orElseThrow().ativo);
    }

    @Test void veiculoCriaListaEEditaComHistorico() throws Exception {
        mvc.perform(post("/veiculos").with(user("gerente").roles("GERENTE")).with(csrf())
                .param("marca", "Toyota").param("modelo", "Etios")
                .param("ano", "2020").param("preco", "51000"))
                .andExpect(status().is3xxRedirection());
        Veiculo v = veiculos.findAll().stream().filter(x -> "Etios".equals(x.modelo)).findFirst().orElseThrow();
        mvc.perform(get("/veiculos").with(user("gerente").roles("GERENTE"))).andExpect(status().isOk());
        mvc.perform(post("/veiculos/" + v.id + "/editar").with(user("gerente").roles("GERENTE")).with(csrf())
                .param("marca", "Toyota").param("modelo", "Etios Plus").param("versao", "1.5")
                .param("cor", "Prata").param("ano", "2020").param("km", "12000")
                .param("categoria", "Hatch").param("cambio", "Manual").param("lugares", "5")
                .param("consumo", "12.5").param("preco", "53000")
                .param("cidade", "Salvador").param("localizacao", "Pátio 1"))
                .andExpect(status().is3xxRedirection());
        assertEquals("Etios Plus", veiculos.findById(v.id).orElseThrow().modelo);
    }

    @Test void oportunidadeCriaListaEAtualizaEtapa() throws Exception {
        Cliente c = cliente("99911122201");
        mvc.perform(post("/oportunidades").with(user("vendedor").roles("VENDEDOR")).with(csrf())
                .param("clienteId", c.id.toString()).param("origem", "Site")
                .param("proximaAcao", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().is3xxRedirection());
        Oportunidade o = oportunidades.findAll().stream().filter(x -> c.id.equals(x.cliente.id)).findFirst().orElseThrow();
        mvc.perform(get("/oportunidades").with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isOk());
        mvc.perform(post("/oportunidades/" + o.id + "/etapa")
                .with(user("gerente").roles("GERENTE")).with(csrf()).param("etapa", "CONTATO"))
                .andExpect(status().is3xxRedirection());
        assertEquals(Oportunidade.Etapa.CONTATO, oportunidades.findById(o.id).orElseThrow().etapa);
    }

    @Test void usuarioCriaListaEInativaComSenhaProtegida() throws Exception {
        mvc.perform(post("/usuarios").with(user("admin@local").roles("ADMIN")).with(csrf())
                .param("nome", "Operador CRUD").param("email", "operador-crud@example.com")
                .param("senha", "SenhaTesteMuitoForte2026!").param("perfil", "VENDEDOR"))
                .andExpect(status().is3xxRedirection());
        Usuario u = usuarios.findByEmail("operador-crud@example.com").orElseThrow();
        assertNotEquals("SenhaTesteMuitoForte2026!", u.senha);
        mvc.perform(get("/usuarios").with(user("admin@local").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(post("/usuarios/" + u.id + "/alternar").with(user("admin@local").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertFalse(usuarios.findById(u.id).orElseThrow().ativo);
    }

    @Test void alertaConsultaEConcluiSemExclusaoFisica() throws Exception {
        Alerta a = new Alerta(); a.tipo = "CONTATO"; a.mensagem = "Ligar para cliente";
        a.responsavel = "vendedor"; a.data = LocalDate.now();
        a = alertas.saveAndFlush(a);
        mvc.perform(get("/alertas").with(user("vendedor").roles("VENDEDOR"))).andExpect(status().isOk());
        mvc.perform(post("/alertas/" + a.id + "/concluir").with(user("vendedor").roles("VENDEDOR")).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertTrue(alertas.findById(a.id).orElseThrow().concluido);
    }

    @Test void perfilCompraCriaConsultaERevisaPorVendedor() throws Exception {
        Cliente c = cliente("99911122202");
        mvc.perform(post("/recomendacao").with(user("vendedor").roles("VENDEDOR")).with(csrf())
                .param("clienteId", c.id.toString()).param("orcamento", "70000"))
                .andExpect(status().isOk());
        PerfilCompra p = perfis.findAll().stream().filter(x -> c.id.equals(x.cliente.id)).findFirst().orElseThrow();
        mvc.perform(get("/recomendacao").with(user("vendedor").roles("VENDEDOR"))).andExpect(status().isOk());
        mvc.perform(post("/recomendacao").with(user("vendedor").roles("VENDEDOR")).with(csrf())
                .param("perfilId", p.id.toString()).param("clienteId", c.id.toString())
                .param("orcamento", "80000")).andExpect(status().isOk());
        assertEquals(0, new BigDecimal("80000").compareTo(perfis.findById(p.id).orElseThrow().orcamento));
    }

    @Test void exclusaoFisicaRestritaAoRepositorioEmRegistroSemDependencias() {
        // Não existe endpoint de exclusão para a maioria dos módulos.
        // Este teste verifica somente o D do repositório, sem sugerir que haja exclusão na UI.
        Alerta a = new Alerta(); a.tipo = "TESTE"; a.mensagem = "Teste de remoção";
        Long id = alertas.saveAndFlush(a).id;
        assertTrue(alertas.findById(id).isPresent());
        alertas.deleteById(id);
        alertas.flush();
        assertTrue(alertas.findById(id).isEmpty());
    }

    @Test void mecanicoNaoPodeCadastrarVeiculoNemUsuario() throws Exception {
        mvc.perform(post("/veiculos").with(user("mecanico").roles("MECANICO")).with(csrf())
                .param("marca", "Fiat").param("modelo", "Uno").param("ano", "2022").param("preco", "40000"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/usuarios").with(user("mecanico").roles("MECANICO")))
                .andExpect(status().isForbidden());
    }
}
