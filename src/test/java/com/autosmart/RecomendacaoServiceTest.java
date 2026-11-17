package com.autosmart;
import com.autosmart.model.Cliente;
import com.autosmart.model.PerfilCompra;
import com.autosmart.model.PesosRecomendacao;
import com.autosmart.model.Recomendacao;
import com.autosmart.model.Veiculo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.PerfilRepo;
import com.autosmart.repository.RecomendacaoHistoricoRepo;
import com.autosmart.repository.VeiculoRepo;
import com.autosmart.service.RecomendacaoService;
import com.autosmart.service.PesosRecomendacaoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test") @Transactional
class RecomendacaoServiceTest {
  @Autowired RecomendacaoService service;
  @Autowired PesosRecomendacaoService configuracoes;

  @Test void administradorPodePersistirPesosValidosESomaErradaEhRejeitada() {
    PesosRecomendacao atual = configuracoes.atuais();
    assertEquals(100, atual.total());
    atual.preco = 25;
    atual.categoria += 5;
    configuracoes.atualizar(atual);
    assertEquals(25, configuracoes.atuais().preco);
    atual.categoria = 99;
    assertThrows(IllegalArgumentException.class, () -> configuracoes.atualizar(atual));
  }

  @Autowired VeiculoRepo veiculos;
  @Autowired ClienteRepo clientes;
  @Autowired PerfilRepo perfis;
  @Autowired RecomendacaoHistoricoRepo historico;

  @Test void ordenaVeiculosEExplicaCriteriosIncluindoFinalidadeEPrioridades() {
    Cliente cliente = new Cliente(); cliente.nome="Teste"; cliente.cpf="10100100105";
    clientes.save(cliente);
    Veiculo bom = new Veiculo(); bom.marca="Marca"; bom.modelo="Economico"; bom.ano=2025;
    bom.preco=new BigDecimal("65000"); bom.categoria="Hatch"; bom.cambio="Manual";
    bom.lugares=5; bom.consumo=14.0; bom.finalidades="Cidade, Família";
    bom.caracteristicas="Câmera de ré, Ar-condicionado"; veiculos.saveAndFlush(bom);
    Veiculo ruim = new Veiculo(); ruim.marca="Marca"; ruim.modelo="Caro"; ruim.ano=2025;
    ruim.preco=new BigDecimal("200000"); ruim.categoria="Picape"; ruim.cambio="Automático";
    ruim.lugares=2; ruim.consumo=8.0; ruim.finalidades="Trabalho";
    ruim.caracteristicas="Tração 4x4"; veiculos.saveAndFlush(ruim);
    Veiculo vendido = new Veiculo(); vendido.marca="Marca"; vendido.modelo="Vendido";
    vendido.preco=new BigDecimal("2000"); vendido.status=Veiculo.Status.VENDIDO; veiculos.save(vendido);

    PerfilCompra p = new PerfilCompra(); p.cliente=cliente; p.orcamento=new BigDecimal("70000");
    p.finalidade="Cidade"; p.categoria="Hatch"; p.cambio="Manual";
    p.ocupantes=4; p.consumoMin=12.0; p.prioridades="Câmera de ré, Econômico";
    perfis.saveAndFlush(p);

    List<Recomendacao> rank = service.recomendar(p);
    assertEquals(2, rank.size()); assertEquals(bom.id, rank.get(0).veiculo().id);
    assertEquals(100, rank.get(0).score());
    assertTrue(rank.get(0).positivos().stream().anyMatch(x -> x.contains("finalidade")));
    assertTrue(rank.get(0).positivos().stream().anyMatch(x -> x.contains("Prioridade")));
    assertFalse(rank.get(1).sem().isEmpty());
    service.registrar(p, rank, "vendedor@autosmart.com");
    assertEquals(2, historico.findByPerfilIdOrderByDataDescClassificacaoAsc(p.id).size());
    assertTrue(historico.findByPerfilIdOrderByDataDescClassificacaoAsc(p.id).get(0).positivos.contains("Categoria"));
  }
  @Test void recusaOrcamentoInvalido() {
    PerfilCompra p = new PerfilCompra(); p.orcamento=BigDecimal.ZERO;
    assertThrows(IllegalArgumentException.class, () -> service.recomendar(p));
  }
}
