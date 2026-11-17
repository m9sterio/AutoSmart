package com.autosmart.service;

import com.autosmart.model.Cliente;
import com.autosmart.model.TestDrive;
import com.autosmart.model.Veiculo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.TestDriveRepo;
import com.autosmart.repository.UsuarioRepo;
import com.autosmart.repository.VeiculoRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

/** Verifica horários disponíveis e registra agendamentos. */
@Service
@Transactional
public class TestDriveService {
    private final TestDriveRepo agendamentos;
    private final UsuarioRepo usuarios;
    private final VeiculoRepo veiculos;
    private final ClienteRepo clientes;
    private final HistoricoService historico;

    public TestDriveService(TestDriveRepo agendamentos, UsuarioRepo usuarios, VeiculoRepo veiculos,
                            ClienteRepo clientes, HistoricoService historico) {
        this.agendamentos = agendamentos;
        this.usuarios = usuarios;
        this.veiculos = veiculos;
        this.clientes = clientes;
        this.historico = historico;
    }

    public ResultadoOperacao agendar(Long veiculoId, Long clienteId, LocalDateTime inicio,
                                     Principal usuario) {
        if (!inicio.isAfter(LocalDateTime.now())) {
            return ResultadoOperacao.erro("Escolha um horário futuro.");
        }

        // O bloqueio evita que dois usuários ocupem a mesma vaga ao mesmo tempo.
        Cliente cliente = clientes.bloquear(clienteId).orElseThrow();
        usuarios.bloquearPorEmail(usuario.getName()).orElseThrow();
        Veiculo veiculo = veiculos.bloquear(veiculoId).orElseThrow();

        if (veiculo.status != Veiculo.Status.DISPONIVEL) {
            return ResultadoOperacao.erro("Veículo indisponível.");
        }
        if (!cliente.ativo) {
            return ResultadoOperacao.erro("Cliente inativo.");
        }

        LocalDateTime inicioJanela = inicio.minusMinutes(60);
        LocalDateTime fimJanela = inicio.plusMinutes(60);
        // Conforme resposta 26 da entrevista: o único recurso que causa conflito é o veículo.
        // O intervalo entre inícios permanece de 60 minutos, conforme a resposta 25.
        boolean existeConflito = agendamentos.conflitoVeiculo(veiculoId, inicioJanela, fimJanela);

        if (existeConflito) {
            return ResultadoOperacao.erro(
                    "Este veículo já tem um test-drive próximo desse horário. Escolha outro horário ou veículo.");
        }

        TestDrive agendamento = new TestDrive();
        agendamento.veiculo = veiculo;
        agendamento.cliente = cliente;
        agendamento.vendedor = usuario.getName();
        agendamento.inicio = inicio;
        agendamentos.save(agendamento);
        historico.audit(usuario, "Test-drive agendado: " + veiculo.descricao());
        return ResultadoOperacao.ok("Test-drive confirmado.");
    }
}
