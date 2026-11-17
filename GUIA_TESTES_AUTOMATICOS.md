# AutoSmart — Automação de testes E2E (Playwright)

## Preparação no Windows

1. Instale **Node.js LTS** (npm) e confirme `node -v` e `npm -v`.
2. Mantenha o PostgreSQL funcionando. No pgAdmin ou psql, **crie um banco novo, vazio e exclusivo** chamado `autosmart_e2e`. **Não renomeie nem apague o banco `autosmart`**.
3. Configure o acesso PostgreSQL da mesma forma que já usa no projeto, por exemplo `$env:DB_USER='postgres'` e `$env:DB_PASSWORD='SUA_SENHA'`. Nunca compartilhe a senha.
4. Abra o PowerShell na pasta `autosmart` e execute `npm install` e `npx playwright install chromium` na primeira utilização.
5. Execute `./INICIAR_TESTES_E2E.ps1 -Visivel` para acompanhar o navegador ou `./INICIAR_TESTES_E2E.ps1` sem janela. Se o PowerShell bloquear o script, use `powershell -ExecutionPolicy Bypass -File .\INICIAR_TESTES_E2E.ps1 -Visivel`.
6. Veja `playwright-report/index.html` ou execute `npx playwright show-report`. Falhas guardam screenshots, vídeos e traces em `test-results/`.

**Segurança:** o script inicia uma instância na porta **8092** com `DB_URL=jdbc:postgresql://localhost:5432/autosmart_e2e`, recusa a porta ocupada e o Playwright só aceita `AUTOSMART_E2E_DB=autosmart_e2e`. Não execute os testes contra o banco principal. Não há necessidade de alterar o banco que você usa no navegador em 8080/8081.

## Cobertura incluída

- Login dos 5 perfis, senha inválida, acesso anônimo, logout, autorização de rotas.
- Carregamento de 10 telas, presença de formulários, viewport mobile, imagem fallback.
- Fluxo integrado: cadastrar cliente, veículo, oportunidade, perfil/recomendação, proposta e test-drive.
- Relatório HTML, JUnit XML, screenshot, vídeo e trace em falhas.

## Limites importantes

- Não há garantia de **todos** os testes possíveis. Testes de concorrência, carga de 50 usuários, acessibilidade detalhada, LGPD, restauração de backup e segurança ofensiva requerem suítes específicas e ambiente controlado.
- As verificações de negócio usam a estrutura atual do AutoSmart; uma falha pode revelar defeito no produto **ou** um seletor do teste que precisa ser atualizado. Verifique os traces.
- Os dados criados durante os testes permanecem no banco `autosmart_e2e`; use apenas esse banco para testes.
- Rode também `mvn clean verify` para os testes Java existentes.
- Não foram executados testes Playwright aqui; o pacote inclui o código e instruções para executá-los no Windows.
