package com.autosmart;
import com.autosmart.model.Cliente;
import com.autosmart.model.Oportunidade;
import com.autosmart.model.Proposta;
import com.autosmart.model.Veiculo;
import com.autosmart.repository.AlertaRepo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.OportunidadeRepo;
import com.autosmart.repository.PropostaRepo;
import com.autosmart.repository.PropostaVersaoRepo;
import com.autosmart.repository.VeiculoRepo;
import com.autosmart.repository.VendaRepo;
import com.autosmart.service.HistoricoService;

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

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class FluxoComercialTest {
  @Autowired MockMvc mvc;
  @Autowired VeiculoRepo veiculos;
  @Autowired ClienteRepo clientes;
  @Autowired PropostaRepo propostas;
  @Autowired PropostaVersaoRepo versoes;
  @Autowired VendaRepo vendas;
  @Autowired AlertaRepo alertas;
  @Autowired OportunidadeRepo oportunidades;
  @Autowired HistoricoService historico;

  @Test void reservaEditaAprovaERegistraVersoesEntregaEAlerta() throws Exception {
    Cliente c = new Cliente(); c.nome="Cliente A"; c.cpf="87610000001"; clientes.save(c);
    Veiculo v = new Veiculo(); v.marca="Fiat"; v.modelo="Teste";
    v.preco=new BigDecimal("90000"); veiculos.saveAndFlush(v);
    String email="vendedor@autosmart.com";
    mvc.perform(post("/propostas").with(user(email).roles("VENDEDOR")).with(csrf())
        .param("clienteId", c.id.toString()).param("veiculoId", v.id.toString())
        .param("desconto", "1000").param("acessorios", "Tapetes")
        .param("validade", LocalDate.now().plusDays(7).toString()))
        .andExpect(status().is3xxRedirection());
    Proposta proposta = propostas.findAll().stream().filter(x -> x.veiculo.id.equals(v.id)).findFirst().orElseThrow();
    assertEquals(Veiculo.Status.RESERVADO, veiculos.findById(v.id).orElseThrow().status);
    assertEquals(1, versoes.countByPropostaId(proposta.id));
    mvc.perform(post("/propostas/" + proposta.id + "/editar")
        .with(user(email).roles("VENDEDOR")).with(csrf())
        .param("desconto", "2000").param("acessorios", "Tapetes e película")
        .param("validade", LocalDate.now().plusDays(8).toString()))
        .andExpect(status().is3xxRedirection());
    assertEquals(2, versoes.countByPropostaId(proposta.id));
    assertEquals(new BigDecimal("88000"), propostas.findById(proposta.id).orElseThrow().valor);
    mvc.perform(post("/propostas/" + proposta.id + "/aprovar")
        .with(user("gerente@autosmart.com").roles("GERENTE")).with(csrf())
        .param("entrega", LocalDate.now().plusDays(3).toString()))
        .andExpect(status().is3xxRedirection());
    assertEquals(Veiculo.Status.VENDIDO, veiculos.findById(v.id).orElseThrow().status);
    assertEquals(3, versoes.countByPropostaId(proposta.id));
    assertEquals(1, vendas.findAll().stream().filter(x -> x.proposta.id.equals(proposta.id)).count());
    assertTrue(alertas.findByConcluidoFalseOrderByData().stream().anyMatch(x -> "PÓS-VENDA".equals(x.tipo)));
  }

  @Test void recusaPropostaLiberaReservaMasDescontoInvalidoNaoReserva() throws Exception {
    Cliente c = new Cliente(); c.nome="Cliente B"; c.cpf="87610000002"; clientes.save(c);
    Veiculo v = new Veiculo(); v.marca="Renault"; v.modelo="Teste";
    v.preco=new BigDecimal("75000"); veiculos.saveAndFlush(v);
    mvc.perform(post("/propostas").with(user("ven").roles("VENDEDOR")).with(csrf())
        .param("clienteId",c.id.toString()).param("veiculoId",v.id.toString())
        .param("desconto","80000").param("validade",LocalDate.now().plusDays(4).toString()))
        .andExpect(status().is3xxRedirection());
    assertEquals(Veiculo.Status.DISPONIVEL, veiculos.findById(v.id).orElseThrow().status);
    assertTrue(propostas.findAll().stream().noneMatch(x -> x.veiculo.id.equals(v.id)));
    mvc.perform(post("/propostas").with(user("ven").roles("VENDEDOR")).with(csrf())
        .param("clienteId",c.id.toString()).param("veiculoId",v.id.toString())
        .param("desconto","0").param("validade",LocalDate.now().plusDays(4).toString()))
        .andExpect(status().is3xxRedirection());
    Proposta proposta = propostas.findAll().stream().filter(x -> x.veiculo.id.equals(v.id)).findFirst().orElseThrow();
    mvc.perform(post("/propostas/"+proposta.id+"/recusar")
        .with(user("ger").roles("GERENTE")).with(csrf()))
        .andExpect(status().is3xxRedirection());
    assertEquals(Veiculo.Status.DISPONIVEL, veiculos.findById(v.id).orElseThrow().status);
  }

  @Test void alertaComercialAcompanhaOportunidadeSemDuplicacao() throws Exception {
    Cliente c = new Cliente(); c.nome="Cliente C"; c.cpf="87610000003"; clientes.save(c);
    mvc.perform(post("/oportunidades").with(user("vendedor@autosmart.com").roles("VENDEDOR")).with(csrf())
        .param("clienteId",c.id.toString()).param("origem","Site")
        .param("proximaAcao",LocalDate.now().plusDays(2).toString()))
        .andExpect(status().is3xxRedirection());
    Oportunidade op = oportunidades.findAll().stream().filter(o -> o.cliente.id.equals(c.id)).findFirst().orElseThrow();
    mvc.perform(post("/oportunidades/"+op.id+"/etapa")
        .with(user("vendedor@autosmart.com").roles("VENDEDOR")).with(csrf())
        .param("etapa","CONTATO").param("proximaAcao",LocalDate.now().plusDays(4).toString()))
        .andExpect(status().is3xxRedirection());
    assertEquals(1, alertas.findByConcluidoFalseOrderByData().stream()
        .filter(a -> "OPORTUNIDADE".equals(a.origemTipo) && op.id.equals(a.origemId)).count());
  }
}
