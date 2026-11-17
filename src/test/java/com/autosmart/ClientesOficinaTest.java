package com.autosmart;
import com.autosmart.model.Cliente;
import com.autosmart.model.OrdemServico;
import com.autosmart.model.Veiculo;
import com.autosmart.repository.AlertaRepo;
import com.autosmart.repository.ClienteHistoricoRepo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.OrdemRepo;
import com.autosmart.repository.VeiculoRepo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class ClientesOficinaTest {
  @Autowired MockMvc mvc;
  @Autowired ClienteRepo clientes;
  @Autowired ClienteHistoricoRepo historicoCliente;
  @Autowired OrdemRepo ordens;
  @Autowired VeiculoRepo veiculos;
  @Autowired AlertaRepo alertas;

  @Test void registroEdicaoInativacaoETrilhaDeAlteracoes() throws Exception {
    mvc.perform(post("/clientes").with(user("vendedor").roles("VENDEDOR")).with(csrf())
        .param("nome", "Cliente Teste").param("cpf", "99101010101")
        .param("cidade", "Salvador").param("ativo", "false")
        .param("id", "12345"))
        .andExpect(status().is3xxRedirection());
    Cliente cliente = clientes.findAll().stream().filter(c -> "99101010101".equals(c.cpf)).findFirst().orElseThrow();
    assertTrue(cliente.ativo); // Nunca confiar em campos sensíveis enviados pelo navegador.
    assertFalse(Long.valueOf(12345).equals(cliente.id));
    mvc.perform(post("/clientes/" + cliente.id + "/editar")
        .with(user("vendedor").roles("VENDEDOR")).with(csrf())
        .param("nome", "Cliente Editado").param("telefone", "71999999999")
        .param("email", "cliente@example.com").param("cidade", "Camaçari"))
        .andExpect(status().is3xxRedirection());
    mvc.perform(post("/clientes/" + cliente.id + "/inativar")
        .with(user("vendedor").roles("VENDEDOR")).with(csrf()))
        .andExpect(status().is3xxRedirection());
    assertFalse(clientes.findById(cliente.id).orElseThrow().ativo);
    assertEquals(3, historicoCliente.findByClienteIdOrderByDataDesc(cliente.id).size());
  }

  @Test void mecanicoAtualizaEConsultorEncerraOSComAlerta() throws Exception {
    Cliente cliente = new Cliente(); cliente.nome="Cliente Oficina"; cliente.cpf="99202020202";
    clientes.save(cliente);
    Veiculo carro = new Veiculo(); carro.marca="Fiat"; carro.modelo="Uno";
    carro.preco = new BigDecimal("60000"); veiculos.save(carro);
    mvc.perform(post("/oficina").with(user("consultor").roles("CONSULTOR")).with(csrf())
        .param("clienteId",cliente.id.toString()).param("veiculoId",carro.id.toString())
        .param("descricao","Revisão periódica"))
        .andExpect(status().is3xxRedirection());
    OrdemServico os = ordens.findAll().stream().filter(o -> o.cliente.id.equals(cliente.id)).findFirst().orElseThrow();
    mvc.perform(post("/oficina/"+os.id+"/atualizar")
        .with(user("mecanico").roles("MECANICO")).with(csrf())
        .param("diagnostico","Óleo vencido").param("atividades","Troca de óleo")
        .param("pecas","Óleo e filtro").param("valor","180.00"))
        .andExpect(status().is3xxRedirection());
    assertEquals(OrdemServico.Status.EM_ANDAMENTO, ordens.findById(os.id).orElseThrow().status);
    mvc.perform(post("/oficina/"+os.id+"/encerrar")
        .with(user("consultor").roles("CONSULTOR")).with(csrf()))
        .andExpect(status().is3xxRedirection());
    assertEquals(OrdemServico.Status.ENCERRADA, ordens.findById(os.id).orElseThrow().status);
    assertTrue(alertas.findByConcluidoFalseOrderByData().stream().anyMatch(a -> "PÓS-SERVIÇO".equals(a.tipo)));
  }
  @Test void naoEncerraOsSemDiagnosticoEServicos() throws Exception {
    Cliente cliente = new Cliente(); cliente.nome="Cliente OS incompleta"; cliente.cpf="99303030303";
    clientes.save(cliente);
    Veiculo veiculo = new Veiculo(); veiculo.marca="Fiat"; veiculo.modelo="Cronos";
    veiculo.preco = new BigDecimal("50000"); veiculos.save(veiculo);
    OrdemServico os = new OrdemServico(); os.cliente=cliente; os.veiculo=veiculo;
    os.descricao="Reparo"; os.valor=BigDecimal.ZERO; ordens.saveAndFlush(os);
    mvc.perform(post("/oficina/"+os.id+"/encerrar")
        .with(user("consultor").roles("CONSULTOR")).with(csrf()))
        .andExpect(status().is3xxRedirection());
    assertEquals(OrdemServico.Status.ABERTA, ordens.findById(os.id).orElseThrow().status);
  }

  @Test void cpfDuplicadoIndicaClienteExistenteSemCriarOutro() throws Exception {
    Cliente c = new Cliente(); c.nome="Já cadastrado"; c.cpf="99404040404";
    clientes.saveAndFlush(c);
    long total = clientes.count();
    mvc.perform(post("/clientes")
        .with(user("vendedor").roles("VENDEDOR")).with(csrf())
        .param("nome", "Outro nome").param("cpf", c.cpf))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("clienteExistenteId", c.id));
    assertEquals(total, clientes.count());
  }
}
