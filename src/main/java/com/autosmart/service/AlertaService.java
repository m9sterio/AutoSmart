package com.autosmart.service;

import com.autosmart.model.Alerta;
import com.autosmart.model.Oportunidade;
import com.autosmart.repository.AlertaRepo;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class AlertaService {
    private final AlertaRepo alertas;
    AlertaService(AlertaRepo repo) {
        this.alertas = repo;
    }
    /** Replaneja o mesmo alerta ao mudar a oportunidade, sem multiplicar notificações. */
    public void oportunidade(Oportunidade o) {
        Alerta alerta = alertas.findByOrigemTipoAndOrigemId("OPORTUNIDADE", o.id).orElseGet(() -> {
            Alerta novo = new Alerta(); novo.origemTipo = "OPORTUNIDADE"; novo.origemId = o.id; return novo;
        });
        if (o.etapa == Oportunidade.Etapa.FECHADO || o.etapa == Oportunidade.Etapa.PERDIDO) {
            // Nenhuma conclusão automática: a confirmação humana continua obrigatória.
            alerta.mensagem = "Verificar encerramento da oportunidade #" + o.id + " de " + o.cliente.nome;
        }
        else {
            alerta.mensagem = "Próxima ação da oportunidade #" + o.id + " (" + o.etapa + "): " + o.cliente.nome;
        }
        alerta.tipo = "COMERCIAL"; alerta.data = o.proximaAcao == null ? LocalDate.now() : o.proximaAcao; alerta.responsavel = o.responsavel; // Alterar próxima ação reabre uma pendência já concluída; a conclusão anterior
        // continua registrada na trilha de auditoria.
        alerta.concluido = false; alertas.save(alerta);
    }
}
