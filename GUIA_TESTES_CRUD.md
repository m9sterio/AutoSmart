# Testes de CRUD — AutoSmart

## Rodar testes isolados de CRUD (H2)

```powershell
mvn "-Dtest=CrudCompletoTest" test
```

Para executar **todos** os testes Java:

```powershell
mvn clean verify
```

Os testes Java usam o perfil `test`, banco H2 em memória e transações com rollback. Eles **não usam o PostgreSQL principal**.

## Cobertura desta suíte

| Módulo | Criação | Leitura | Atualização | Exclusão ou encerramento |
|---|---|---|---|---|
| Clientes | Sim | Sim | Sim | Inativação |
| Veículos | Sim | Sim | Sim | Sem rota de exclusão |
| Oportunidades | Sim | Sim | Etapa | Sem rota de exclusão |
| Usuários | Sim | Sim | Ativação/inativação | Inativação |
| Alertas | Criado como dado de teste | Sim | Conclusão | Conclusão |
| Perfil de compra | Sim | Sim | Revisão | Sem rota de exclusão |
| Alerta via repositório | Sim | Sim | — | Exclusão física isolada |

Os testes existentes (`ClientesOficinaTest`, `FluxoComercialTest`, `AgendamentoTest`, etc.) também cobrem oficina, propostas, agendamentos, restrições e segurança. Em módulos onde não existe operação de exclusão na aplicação, **não há CRUD completo de interface**. O teste isolado de remoção física do repositório não equivale a uma tela de exclusão.

## Playwright

O conjunto original de 34 testes E2E continua disponível. Rode-o **somente com `autosmart_e2e`**, jamais com o banco principal:

```powershell
powershell -ExecutionPolicy Bypass -File .\INICIAR_TESTES_E2E.ps1 -Visivel
```

O resultado desta nova suíte Java precisa ser executado na sua máquina: o ZIP foi montado com os testes, mas não se deve declarar aprovação sem rodar `mvn clean verify`.
