const { test, expect } = require('@playwright/test');
const { login, tomorrow } = require('./helpers');
const prefix = 'E2E' + Date.now().toString().slice(-8);
const cliente = `${prefix} Cliente Teste`;
const modelo = `${prefix} Modelo`;
test.describe.serial('fluxo real com dados isolados', () => {
  // O POST de recomendação exige ROLE_VENDEDOR na versão corrigida.
  // Os demais fluxos continuam com admin, conforme suas próprias permissões.
  test.beforeEach(async ({page}, testInfo) => {
    const perfil = testInfo.title === 'registrar perfil e obter recomendação'
      ? 'vendedor'
      : 'admin';
    await login(page, perfil);
  });
  test('cadastrar cliente e localizar na tabela', async ({page}) => {
    await page.goto('/clientes');
    const f=page.locator('form[action="/clientes"]');
    await f.locator('[name=nome]').fill(cliente);
    await f.locator('[name=cpf]').fill('E2E-'+prefix);
    await f.locator('[name=telefone]').fill('(71) 99999-1111');
    await f.locator('[name=email]').fill(`${prefix.toLowerCase()}@example.test`);
    await f.locator('button').click();
    await expect(page.locator('table')).toContainText(cliente);
  });
  test('cadastrar veículo e localizar no estoque', async ({page}) => {
    await page.goto('/veiculos'); const f=page.locator('form[action="/veiculos"]');
    const fields={marca:'Teste',modelo,versao:'E2E',ano:'2024',cor:'Prata',km:'12000',lugares:'5',consumo:'12.5',localizacao:'Pátio E2E',preco:'125000',finalidades:'Cidade',caracteristicas:'Ar-condicionado'};
    for (const [name,value] of Object.entries(fields)) await f.locator(`[name=${name}]`).fill(value);
    await f.locator('[name=categoria]').selectOption({label:'Sedan'});
    await f.locator('[name=cambio]').selectOption({label:'Automático'});
    await f.locator('button').click();
    await expect(page.locator('table')).toContainText(modelo);
  });
  test('criar oportunidade e conferir listagem', async ({page}) => {
    await page.goto('/oportunidades'); const f=page.locator('form[action="/oportunidades"]');
    await f.locator('[name=clienteId]').selectOption({label:cliente});
    await f.locator('[name=origem]').selectOption({label:'Site'});
    await f.locator('[name=observacoes]').fill(`Oportunidade ${prefix}`);
    await f.locator('[name=proximaAcao]').fill(tomorrow());
    await f.locator('button').click();
    await expect(page.locator('table')).toContainText(`Oportunidade ${prefix}`);
  });
  test('registrar perfil e obter recomendação', async ({page}) => {
    await page.goto('/recomendacao'); const f=page.locator('form[action="/recomendacao"]');
    await f.locator('[name=clienteId]').selectOption({label:cliente});
    await f.locator('[name=orcamento]').fill('150000');
    await f.locator('[name=finalidade]').selectOption({label:'Cidade'});
    await f.locator('[name=ocupantes]').fill('4');
    await f.locator('[name=categoria]').selectOption({label:'Sedan'});
    await f.locator('[name=cambio]').selectOption({label:'Automático'});
    await f.locator('[name=consumoMin]').fill('10');
    await f.locator('[name=prioridades]').fill('Ar-condicionado');
    await f.locator('button').click();
    await expect(page.locator('body')).toContainText(cliente);
    await expect(page.locator('body')).toContainText(/pontua|recomend|compat/i);
  });
  test('agendar test-drive e verificar horário', async ({page}) => {
    // Cada execução pode deixar agendamentos no banco de testes. Não reutilize
    // sempre o mesmo horário: o sistema bloqueia corretamente conflitos.
    let confirmado = false;
    for (let tentativa = 0; tentativa < 6; tentativa++) {
      await page.goto('/testdrives');
      const f = page.locator('form[action="/testdrives"]');
      await f.locator('[name=clienteId]').selectOption({label:cliente});
      await f.locator('[name=veiculoId]').evaluate((el, name) => {
        const opt = [...el.options].find(o => o.text.includes(name));
        if (!opt) throw new Error('Veículo E2E não está disponível na lista');
        el.value = opt.value;
        el.dispatchEvent(new Event('change', {bubbles:true}));
      }, modelo);
      // A data depende da execução e muda a cada tentativa caso haja conflito.
      const dias = 30 + (Number(prefix.slice(3)) % 250) + tentativa * 19;
      const data = tomorrow(dias);
      const horario = `${data}T15:00`;
      await f.locator('[name=inicio]').fill(horario);
      await f.locator('button').click();
      if (await page.getByText('Test-drive confirmado.', {exact:true}).isVisible()) {
        const linha = page.locator('table tr').filter({hasText: cliente});
        await expect(linha).toContainText(modelo);
        const [ano, mes, dia] = data.split('-');
        await expect(linha).toContainText(`${dia}/${mes}/${ano} 15:00`);
        confirmado = true;
        break;
      }
      // Só tente outro horário se for o bloqueio esperado por conflito.
      await expect(page.locator('.err')).toContainText('Conflito de horário');
    }
    expect(confirmado, 'Nenhum dos seis horários alternativos ficou livre').toBeTruthy();
  });
  test('criar proposta e verificar status', async ({page}) => {
    await page.goto('/propostas'); const f=page.locator('form[action="/propostas"]');
    await f.locator('[name=clienteId]').selectOption({label:cliente});
    await f.locator('[name=veiculoId]').evaluate((el, name) => { const opt = [...el.options].find(o => o.text.includes(name)); if (!opt) throw new Error('Veículo E2E não está disponível na lista'); el.value = opt.value; el.dispatchEvent(new Event('change', {bubbles:true})); }, modelo);
    await f.locator('[name=desconto]').fill('1000');
    await f.locator('[name=acessorios]').fill(`Tapetes ${prefix}`);
    await f.locator('[name=validade]').fill(tomorrow(10));
    await f.locator('button').click();
    await expect(page.locator('table')).toContainText(cliente);
  });

});
