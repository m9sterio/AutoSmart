# Organização MVC do AutoSmart

O projeto usa Spring Boot, Thymeleaf e PostgreSQL. A organização foi feita para deixar
mais fácil encontrar cada parte do código sem mudar as telas e as funcionalidades.

## Onde fica cada coisa?

```text
autosmart/
├── src/main/java/com/autosmart/
│   ├── AutoSmartApplication.java      # inicia o Spring Boot
│   ├── config/                        # segurança, dados iniciais e configurações
│   ├── controller/                    # recebe requisições e escolhe as páginas
│   ├── model/                         # entidades do banco e dados do sistema
│   ├── repository/                    # interfaces do Spring Data JPA
│   └── service/                       # regras do sistema
├── src/main/resources/
│   ├── templates/                     # telas HTML com Thymeleaf (View)
│   ├── static/css/                    # estilo do site
│   ├── static/img/veiculos/           # imagens locais dos carros
│   └── application.properties         # configuração do Spring
├── src/test/java/com/autosmart/       # testes Java
└── e2e/                               # testes de navegador (Playwright)
```

## Como MVC funciona aqui?

- **Model:** representa os dados. Ex.: `Cliente`, `Veiculo`, `Proposta`, `TestDrive`.
- **View:** é a tela que a pessoa usa. Ex.: `veiculos.html`, `propostas.html`.
- **Controller:** recebe os formulários e devolve a página ou um redirecionamento.
- **Service:** aplica regras que não devem ficar espalhadas nas páginas. Os fluxos de
  propostas/vendas e de agendamento foram separados em `PropostaService` e
  `TestDriveService`. O cálculo de recomendações fica em `RecomendacaoService`.
- **Repository:** conversa com o banco usando Spring Data JPA. Cada interface
  está em seu próprio arquivo, como `VeiculoRepo.java` e `ClienteRepo.java`.

### Exemplo: criando uma proposta

1. O usuário preenche a tela `propostas.html`.
2. O formulário envia os dados para `PropostaController`.
3. O controller chama `PropostaService.criar(...)`.
4. O service confere as regras de desconto, disponibilidade e validade.
5. Os repositories salvam a proposta e o histórico no banco.
6. O usuário volta para a página de propostas com uma mensagem.

O mesmo raciocínio vale para os agendamentos, que passam por `TestDriveService`.

## O que foi mantido?

As rotas (`/clientes`, `/veiculos`, `/propostas`, `/testdrives` etc.), os formulários,
os nomes das tabelas e as regras de permissões não foram alterados de propósito.
As 24 imagens ilustrativas locais continuam no projeto, com preferência por uma
foto personalizada quando ela já estiver cadastrada no banco.

## Executando e testando

Abra o PowerShell na pasta `autosmart`, configure o PostgreSQL como no `README.md`
e execute:

```powershell
$env:DB_PASSWORD="SENHA_DO_POSTGRES"
$env:AUTOSMART_DEMO_ENABLED="true"
mvn spring-boot:run
```

Abra `http://localhost:8080`.

Para verificar os testes Java no seu computador:

```powershell
mvn clean verify
```

O Playwright continua configurado para usar um **banco isolado de testes**, nunca o
banco principal. Consulte `GUIA_TESTES_AUTOMATICOS.md` para esse procedimento.

## Para continuar desenvolvendo

Se precisar adicionar outra funcionalidade, mantenha um arquivo por classe.
Coloque a rota HTTP no `controller`, os dados no `model`, as consultas no
`repository` e as validações mais importantes no `service`. Prefira métodos
pequenos e nomes que deixem claro o que fazem. Comente somente quando a intenção
não for óbvia pelo próprio código.
