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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class AgendamentoTest {
  @Autowired MockMvc mvc;
  @Autowired VeiculoRepo veiculos;
  @Autowired ClienteRepo clientes;
  @Autowired UsuarioRepo usuarios;
  @Autowired TestDriveRepo drives;

  @Test void bloqueiaApenasMesmoVeiculoEPermiteOutroCarroNoMesmoHorario() throws Exception {
    Usuario vendedor = new Usuario(); vendedor.email="agendamento@autosmart.com";
    vendedor.nome="Vendedor Agendamento"; vendedor.senha="hash";
    vendedor.perfil=Usuario.Perfil.VENDEDOR; usuarios.save(vendedor);
    Cliente cliente = new Cliente(); cliente.nome="Cliente"; cliente.cpf="88800000001"; clientes.save(cliente);
    Veiculo v = new Veiculo(); v.marca="VW"; v.modelo="Polo";
    v.preco=BigDecimal.TEN; veiculos.save(v);
    Veiculo outro = new Veiculo(); outro.marca="Fiat"; outro.modelo="Cronos";
    outro.preco=BigDecimal.TEN; veiculos.save(outro);
    String hora=LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0).toString();
    mvc.perform(post("/testdrives").with(user(vendedor.email).roles("VENDEDOR")).with(csrf())
        .param("clienteId", cliente.id.toString()).param("veiculoId", v.id.toString())
        .param("inicio", hora)).andExpect(status().is3xxRedirection());
    // Mesmo veículo e intervalo inferior a 60 minutos: bloqueado.
    mvc.perform(post("/testdrives").with(user(vendedor.email).roles("VENDEDOR")).with(csrf())
        .param("clienteId", cliente.id.toString()).param("veiculoId", v.id.toString())
        .param("inicio", LocalDateTime.parse(hora).plusMinutes(30).toString()))
        .andExpect(status().is3xxRedirection());
    assertEquals(1, drives.count());
    // Mesmo vendedor/cliente em outro veículo no mesmo horário: autorizado pela entrevista.
    mvc.perform(post("/testdrives").with(user(vendedor.email).roles("VENDEDOR")).with(csrf())
        .param("clienteId",cliente.id.toString()).param("veiculoId",outro.id.toString())
        .param("inicio",hora)).andExpect(status().is3xxRedirection());
    assertEquals(2, drives.count());
    // Exatamente 60 minutos entre os inícios: não é conflito.
    mvc.perform(post("/testdrives").with(user(vendedor.email).roles("VENDEDOR")).with(csrf())
        .param("clienteId",cliente.id.toString()).param("veiculoId",v.id.toString())
        .param("inicio",LocalDateTime.parse(hora).plusHours(1).toString()))
        .andExpect(status().is3xxRedirection());
    assertEquals(3, drives.count());
  }
}
