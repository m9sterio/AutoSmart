package com.autosmart;
import com.autosmart.model.Cliente;
import com.autosmart.model.Usuario;
import com.autosmart.model.Veiculo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.TestDriveRepo;
import com.autosmart.repository.UsuarioRepo;
import com.autosmart.repository.VeiculoRepo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
class AgendamentoConcorrenteTest {
  @Autowired MockMvc mvc;
  @Autowired UsuarioRepo usuarios;
  @Autowired VeiculoRepo veiculos;
  @Autowired ClienteRepo clientes;
  @Autowired TestDriveRepo drives;

  @Test void duasSolicitacoesSimultaneasParaMesmoVeiculoGeramUmAgendamento() throws Exception {
    for (String email : new String[]{"conc1@autosmart.com", "conc2@autosmart.com"}) {
      Usuario u = new Usuario(); u.nome=email; u.email=email; u.senha="hash";
      u.perfil=Usuario.Perfil.VENDEDOR; usuarios.save(u);
    }
    Cliente c = new Cliente(); c.nome="Cliente concorrência"; c.cpf="10110110101"; clientes.save(c);
    Veiculo v = new Veiculo(); v.marca="VW"; v.modelo="Concorrência"; v.preco=BigDecimal.TEN;
    veiculos.save(v);
    String hora = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0).toString();
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch iniciar = new CountDownLatch(1);
    CountDownLatch prontos = new CountDownLatch(2);
    AtomicInteger pedidosConcluidos = new AtomicInteger();
    try {
      Future<?>[] tarefas = new Future<?>[2];
      for (int i=0;i<2;i++) {
        final String email = "conc" + (i+1) + "@autosmart.com";
        tarefas[i] = pool.submit(() -> {
          try {
            prontos.countDown(); iniciar.await();
            mvc.perform(post("/testdrives").with(user(email).roles("VENDEDOR")).with(csrf())
                .param("clienteId",c.id.toString()).param("veiculoId",v.id.toString())
                .param("inicio",hora));
            pedidosConcluidos.incrementAndGet();
          } catch (Exception ex) { throw new RuntimeException(ex); }
          return null;
        });
      }
      assertTrue(prontos.await(10, TimeUnit.SECONDS)); iniciar.countDown();
      for (Future<?> future : tarefas) future.get(20, TimeUnit.SECONDS);
      assertEquals(2, pedidosConcluidos.get());
      assertEquals(1, drives.findAll().stream().filter(t -> t.veiculo.id.equals(v.id)).count());
    } finally { iniciar.countDown(); pool.shutdownNow(); }
  }

  @Test void doisVendedoresCarrosDiferentesPodemAgendarMesmoClienteNoMesmoHorario() throws Exception {
    String[] emails={"outro1@autosmart.com", "outro2@autosmart.com"};
    for (String email : emails) {
      Usuario u=new Usuario(); u.nome=email; u.email=email; u.senha="hash";
      u.perfil=Usuario.Perfil.VENDEDOR; usuarios.save(u);
    }
    Cliente c = new Cliente(); c.nome="Cliente mesma hora"; c.cpf="10110110102"; clientes.save(c);
    Long[] ids = new Long[2];
    for (int i=0; i<2; i++) {
      Veiculo v = new Veiculo(); v.marca="Marca"; v.modelo="Outro"+i; v.preco=BigDecimal.TEN;
      ids[i]=veiculos.save(v).id;
    }
    String hora=LocalDateTime.now().plusDays(3).withHour(11).withMinute(0).withSecond(0).withNano(0).toString();
    ExecutorService pool=Executors.newFixedThreadPool(2);
    CountDownLatch start=new CountDownLatch(1);
    try {
      Future<?>[] futures=new Future<?>[2];
      for (int i=0; i<2; i++) {
        final int pos=i;
        futures[i]=pool.submit(() -> {
          try {
            start.await();
            mvc.perform(post("/testdrives").with(user(emails[pos]).roles("VENDEDOR")).with(csrf())
                .param("clienteId",c.id.toString()).param("veiculoId",ids[pos].toString())
                .param("inicio",hora));
          } catch(Exception ex) { throw new RuntimeException(ex); }
          return null;
        });
      }
      start.countDown();
      for (Future<?> f : futures) f.get(20,TimeUnit.SECONDS);
      assertEquals(2,drives.findAll().stream().filter(t -> t.cliente.id.equals(c.id)).count());
    } finally { start.countDown(); pool.shutdownNow(); }
  }

}
