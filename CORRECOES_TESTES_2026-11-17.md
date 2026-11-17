# Correções dos testes E2E — 17/11/2026

## Causa do erro reportado

O teste `registrar perfil e obter recomendação` enviava POST `/recomendacao` como `admin`, embora `SecurityConfig` permita a operação apenas a `VENDEDOR`; resposta: HTTP 403.

## Ajustes realizados

1. `e2e/03-fluxos.spec.js`: login como `vendedor` somente nesse cenário; demais cenários continuam como `admin`.
2. `playwright.config.js`: retorna a URL-base para `http://127.0.0.1:8092`, alinhada ao script de testes. Usar a porta 8080 poderia alcançar a instância do banco normal.
3. `README.md`: instruções para executar Maven e Playwright sem misturar banco principal com `autosmart_e2e`.
4. O ZIP não inclui caches, relatórios, `node_modules` ou `target`.

## Limites da validação

Sintaxe JavaScript e integridade do arquivo ZIP verificadas na montagem do pacote. Os testes completos Maven e Playwright precisam ser executados no Windows com Maven, navegador e banco de testes em execução. Não afirmar 34/34 aprovados antes do novo resultado.
