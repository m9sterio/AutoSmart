# AutoSmart — Spring Boot / Thymeleaf / PostgreSQL

Versão corrigida de acordo com `ATIVIDADE_FINALIZADA.pdf` (Fase 2, RF01–RF15).
Java 17+; Maven 3.9+; PostgreSQL 16 (ou banco PostgreSQL compatível).

## Estrutura MVC

O código Java está organizado em `controller`, `model`, `repository`, `service` e `config`.
As telas continuam em `src/main/resources/templates` e as imagens dos veículos ficam
em `src/main/resources/static/img/veiculos`.

Veja o arquivo [ESTRUTURA_MVC.md](ESTRUTURA_MVC.md) para entender o fluxo das
requisições e onde alterar cada funcionalidade.

## Como executar no Windows (PowerShell)

1. Instale Java, Maven e Docker Desktop (ou PostgreSQL). Copie `.env.example` para `.env` e altere `DB_PASSWORD` para uma senha forte.
2. Inicie o banco no diretório do projeto: `docker compose up -d`.
3. No PowerShell que iniciará o Spring Boot, configure **a mesma** senha:

```powershell
$env:DB_PASSWORD="a-senha-do-seu-arquivo-.env"
# Ambiente de demonstração isolado (cria contas conhecidas e registros fictícios)
$env:AUTOSMART_DEMO_ENABLED="true"
mvn spring-boot:run
```

4. Acesse `http://localhost:8080` (vitrine) ou `/login` (funcionários). Healthcheck: `/actuator/health`.

> **Não use contas demonstrativas nem senhas conhecidas em produção.** Por segurança,
> `AUTOSMART_DEMO_ENABLED` é **false** por padrão. Em banco novo de produção, informe
> `AUTOSMART_ADMIN_EMAIL` e `AUTOSMART_ADMIN_PASSWORD` (no mínimo 12 caracteres)
> para criar o primeiro administrador. A senha é salva apenas como hash BCrypt.
> Não inclua essas variáveis em arquivos versionados.

## Perfis de teste — somente quando DEMO estiver ativo

| Perfil | Usuário | Senha |
|---|---|---|
| Administrador | admin@autosmart.com | admin123 |
| Gerente | gerente@autosmart.com | gerente123 |
| Vendedor | vendedor@autosmart.com | vendedor123 |
| Consultor | consultor@autosmart.com | consultor123 |
| Mecânico | mecanico@autosmart.com | mecanico123 |

O módulo comercial não fica disponível para Mecânico/Consultor. O cadastro de veículos é
restrito a Administrador/Gerente. Decisões sobre vendas só podem ser confirmadas por esses
perfis; editar proposta é restrito ao criador ou a gestores.

## Testes

```powershell
mvn clean verify
# executa testes JUnit 5, MockMvc e H2 (não altera dados reais do PostgreSQL)
```

Casos em `src/test/java/com/autosmart`: isolamento por perfis e CSRF; cadastro/oficina; ranking e explicação de
recomendações; históricos de propostas; reserva e venda; alertas comerciais; choques de
agendamento e concorrência. GitHub Actions executa `mvn clean verify` a cada push/PR.

Para **carga com 50 usuários** instale o k6 e execute em um banco de teste, NÃO no banco real:

```powershell
$env:BASE_URL="http://localhost:8080"
$env:TEST_USER="vendedor@autosmart.com"
$env:TEST_PASSWORD="vendedor123"
k6 run scripts/performance/carga50.js
```

O script mede respostas HTTP e confirma acessos simultâneos; a integridade das operações
concorrentes é avaliada pelos testes específicos. **Não garante** disponibilidade de 99%,
nem comprova 50 usuários em produção sem rodar a carga no ambiente de destino.

## Backup e restauração (PostgreSQL local)

```powershell
$env:DB_PASSWORD="sua-senha"
.\scripts\backup.ps1
# Restaurar somente com manutenção programada e banco isolado:
.\scripts\restore.ps1 -Arquivo ".\backups\autosmart_YYYYMMDD_HHMMSS.dump" -ConfirmoSobrescrever
```

Requer `pg_dump` e `pg_restore` no `PATH`. Configure o Agendador de Tarefas do Windows para
backups periódicos, com retenção e armazenamento criptografado fora do computador. Teste
restauração e monitore `/actuator/health` para medir disponibilidade real. Backup não foi
programado automaticamente para evitar gravar dados e credenciais sem autorização.

## Observações técnicas

- O esquema é atualizado automaticamente com `spring.jpa.hibernate.ddl-auto=update`,
  conveniente para exercícios; em produção use migrações versionadas e backup prévio.
- A recomendação usa pesos configuráveis em `application.properties` cuja soma deve ser 100.
  Inclui finalidade e prioridades e preserva explicações por resultado.
- Novos históricos passam a ser registrados **a partir desta versão**. A versão anterior de
  registros antigos não pode ser reconstruída se não foi salva no banco.
- O conjunto de testes no ZIP foi **preparado para execução** em máquina com Maven e
  dependências; ver `RELATORIO_CORRECOES.md` para evidências e pendências.


## Testes após a correção de permissões (17/11/2026)

- **JUnit/integracao:** `mvn clean verify` na pasta `autosmart`.
- **Playwright:** use o banco PostgreSQL separado `autosmart_e2e`, configure `DB_USER` e `DB_PASSWORD` localmente e execute `powershell -ExecutionPolicy Bypass -File .\INICIAR_TESTES_E2E.ps1 -Visivel`.
- O script inicia o sistema de testes na porta **8092**, separada da aplicação normal na 8080. Não aponte o Playwright para 8080.
- O teste de recomendação entra como `vendedor` porque somente esse perfil pode executar o POST `/recomendacao`; o acesso admin às demais funções segue inalterado.
- O ZIP **não inclui** `node_modules`, `target`, `test-results` nem `playwright-report`: execute `npm install` na primeira utilização.
