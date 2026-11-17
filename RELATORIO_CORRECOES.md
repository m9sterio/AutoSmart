# AutoSmart — Relatório de correções e validação
**Referência:** `ATIVIDADE_FINALIZADA.pdf` (Fase 1, RF01–RF12, RNF01–RNF08; Fase 2, RF01–RF15, RNF01–RNF15).  
**Data da revisão:** 14/11/2026.  
**Base:** ZIP `autosmart.zip1.zip` enviado pelo usuário.

## 1. Resultado

Foi entregue **código-fonte alterado**, com telas, regras de negócio, testes JUnit e scripts operacionais.
Os requisitos funcionais da **Fase 2** foram contemplados no código, com as ressalvas de
validação abaixo. **Ainda não é possível certificar 100% dos requisitos**, porque não houve
execução integrada real com Maven/Spring Boot/PostgreSQL nem testes de desempenho e disponibilidade.

## 2. Matriz de rastreabilidade — funcionais (Fase 2)

| ID | Correções / evidência no projeto | Avaliação nesta entrega |
|---|---|---|
| RF01 | `SecurityConfig`, acesso por perfil a GET/POST, CSRF, BCrypt, demo desativado por padrão, bootstrap seguro. | Implementado; testes MockMvc preparados. |
| RF02 | `ClienteController`, `ClienteEdicaoController`, inativação sem apagar o registro. | Implementado; testes preparados. |
| RF03 | `ClienteHistorico`, `HistoricoService`, tela `cliente-historico.html`. | Implementado para alterações futuras; histórico anterior não recuperável. |
| RF04 | `VeiculoController`, edição de localização, preço e características; `VeiculoHistorico`. | Implementado para operações novas e futuras. |
| RF05 | `PerfilCompra.prioridades` e revisão de perfil existente em `recomendacao.html`. | Implementado; requer execução da tela. |
| RF06 | `RecomendacaoService`: pesos configuráveis, pontuação e ordenação por score. | Lógica de pontuação verificada com Java puro; integração pendente. |
| RF07 | Fatores positivos, atenção e sem correspondência; registro da justificativa em `RecomendacaoHistorico`. | Lógica verificada; telas pendentes de teste em navegador. |
| RF08 | `OportunidadesController`, origem, etapa, observações, próxima ação, responsável. | Implementado; testes preparados. |
| RF09 | `PropostaController`, edição, `PropostaVersao`, preço base congelado e tela de versões. | Implementado; testes preparados. |
| RF10 | `TestDriveController`: conflitos cliente/veículo/vendedor e bloqueios pessimistas de linhas. | Implementado; testes de concorrência escritos e pendentes de execução integrada. |
| RF11 | Proposta aprovada por gerente/admin gera `Venda` com data de entrega. | Implementado; testes preparados. |
| RF12 | Atualização da situação `DISPONIVEL → RESERVADO → VENDIDO`, com trilha. | Implementado; testes preparados. |
| RF13 | `OficinaController`, abertura, diagnóstico, atividades, peças, valores e fechamento. | Implementado; testes preparados. |
| RF14 | `AlertaService` para oportunidades, alertas de venda/oficina, confirmação humana obrigatória. | Lógica de alerta verificada com Java puro; integração pendente. |
| RF15 | `PainelController` apresenta estoque, vendas, oportunidades, ordens de serviço e alertas. | Implementado; visualização pendente de execução. |

**Importante:** "Implementado" aqui é uma classificação de cobertura **do código**, não um
certificado de aprovação de todos os testes de aceitação do PDF.

## 3. Requisitos não funcionais e pendências

| ID da Fase 2 | Situação nesta entrega | Para validar / finalizar |
|---|---|---|
| RNF01–02 (até 3 s; recomendação em tempo real) | Script `scripts/performance/carga50.js` incluído. | Executar k6 no ambiente-alvo e registrar tempos por operação/recomendação. |
| RNF03–04 (50 usuários e consistência) | Cenários de concorrência JUnit + script com 50 usuários virtuais. | Rodar Maven/k6 e conferir integridade no PostgreSQL. |
| RNF05 (credenciais) | BCrypt, mínimo de 12 caracteres para novas contas, bootstrap administrativo seguro. | Validar teste com Spring Security real; revisar política de senhas e segredo operacional. |
| RNF06 (expiração) | `server.servlet.session.timeout=30m`. | Teste cronometrado da expiração no navegador/servidor. |
| RNF07 (menor privilégio) | Rotas e ações restritas por perfil; nav e painel condicionais. | Executar testes MockMvc e matriz de privilégios. |
| RNF08–09 (privacidade e legislação) | Acesso por perfis, históricos/auditoria. | Completar avaliação LGPD: base legal, retenção, descarte, titular, política de privacidade e segurança de produção. |
| RNF10 (transparência) | Score e fatores explicados, com snapshot preservado. | Validar em navegador e revisar clareza junto a usuários. |
| RNF11–12 (responsividade) | CSS adaptado com regras 375 px, 768 px e desktop. | Testar visualmente nas resoluções 375, 768 e 1920 px. |
| RNF13 (99% ao mês) | Endpoint de saúde `/actuator/health` adicionado. | Definir monitoramento, hospedagem, operação e medir disponibilidade real por mês. |
| RNF14 (backup e restauração) | Scripts PowerShell com salvaguarda contra restauração acidental. | Instalar `pg_dump`/`pg_restore`, agendar backups e testar restauração e retenção. |
| RNF15 (separação de módulos) | Controllers divididos por responsabilidade e serviços independentes para históricos/recomendações/alertas. | Testar regressões após atualizações e introduzir migrações de banco versionadas. |

## 4. O que foi efetivamente testado **neste ambiente**

1. **Compilação de sintaxe e tipos com Java 17 (`javac --release 17`)** dos arquivos principais e classes de teste, usando **stubs artificiais** de Spring/JPA/JUnit para substituir bibliotecas ausentes: **passou**. Isso não equivale a compilar contra as dependências reais do Maven.
2. **12 verificações funcionais executadas** em Java, com repositórios simulados: ordenação de recomendação, score máximo, fatores de prioridade/finalidade, justificativas, histórico, orçamento inválido, atualização de alerta sem duplicação e manutenção da confirmação humana: **passaram**.
3. **Verificação estática de estrutura:** POM/XML válido, HTML com formulários POST vinculados a ações Thymeleaf, ausência de rotas GET/POST duplicadas, pesos somando 100 e arquivos esperados: **passou**.
4. **Testes integrados JUnit/MockMvc/H2:** foram escritos os arquivos `SegurancaWebTest`, `RecomendacaoServiceTest`, `FluxoComercialTest`, `AgendamentoTest`, `AgendamentoConcorrenteTest` e `ClientesOficinaTest`; **não executados** neste ambiente.

### Bloqueio encontrado

O ambiente de execução usado nesta revisão tem Java, mas **não possui Maven**, dependências
Spring baixadas nem acesso DNS ao Maven Central. Portanto, **não consegui executar**
`mvn clean verify`, iniciar o Spring Boot ou fazer testes com PostgreSQL e navegador.
Essa etapa deve ser feita na máquina do usuário ou pelo workflow CI do GitHub Actions,
preferencialmente antes de qualquer apresentação/implantação.

## 5. Como testar na máquina do usuário

1. Configure Java 17+ e Maven 3.9+ (ou use Maven pelo IntelliJ/Eclipse), PostgreSQL 16.
2. Configure `DB_PASSWORD` e a inicialização conforme `README.md`. **Não use dados reais com DEMO ativado.**
3. Em um terminal dentro de `autosmart`, execute `mvn clean verify`.
4. Com a aplicação executando, confirme os fluxos cliente → perfil → recomendação → test-drive → proposta → decisão humana → venda; realize também o fluxo de OS e alertas.
5. Confira os perfis ADMIN/GERENTE/VENDEDOR/CONSULTOR/MECANICO: usuário sem autorização deve receber 403 e não visualizar menus protegidos.
6. Rode o teste k6 com 50 usuários em base exclusiva de teste; confira tempos e integridade das reservas e vendas.
7. Valide backup/restore, expiração de sessão e telas em 375/768/1920 px.

## 6. Limitações importantes

- O `ddl-auto=update` é útil em laboratório; **faça backup do banco anterior** antes de
  iniciar o projeto corrigido e prefira migrações versionadas (Flyway/Liquibase) na produção.
- Históricos antigos não podem ser recuperados retrospectivamente se não eram registrados.
- **Não foi demonstrada** disponibilidade de 99%, cumprimento da LGPD ou latência de até 3 segundos.
- As telas não foram submetidas a testes de navegador. Mudanças em DTOs, URLs, banco, templates,
  CSS e configurações devem ser confirmadas pelo build integrado.
- O código não deve ser considerado pronto para produção sem esses testes, hardening e políticas operacionais.
