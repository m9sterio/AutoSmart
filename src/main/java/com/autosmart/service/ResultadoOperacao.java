package com.autosmart.service;

/** Resposta simples para mostrar uma mensagem após salvar um formulário. */
public record ResultadoOperacao(boolean sucesso, String mensagem) {
    public static ResultadoOperacao ok(String mensagem) {
        return new ResultadoOperacao(true, mensagem);
    }

    public static ResultadoOperacao erro(String mensagem) {
        return new ResultadoOperacao(false, mensagem);
    }
}
