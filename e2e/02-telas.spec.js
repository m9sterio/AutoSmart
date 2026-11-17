const { test, expect } = require('@playwright/test');
const { login, go } = require('./helpers');
const routes = [
  ['/painel','Painel'], ['/clientes','Clientes'], ['/veiculos','Estoque'],
  ['/oportunidades','Oportunidades'], ['/recomendacao','Recomendação'],
  ['/testdrives','Test-drives'], ['/propostas','Propostas'],
  ['/oficina','Oficina'], ['/alertas','Alertas'], ['/usuarios','Usuários']
];
test.beforeEach(async ({page})=>login(page));
for (const [path, title] of routes) {
  test(`tela ${path} carrega sem erro HTTP`, async ({page}) => {
    const response=await page.goto(path); expect(response.status()).toBe(200);
    await expect(page.locator('h1')).toContainText(title);
  });
}
test('formulários principais existem', async ({page}) => {
  for (const path of ['/clientes','/veiculos','/oportunidades','/recomendacao','/testdrives','/propostas','/oficina']) {
    await page.goto(path); await expect(page.locator('main form').first()).toBeVisible();
  }
});
test('interface em viewport mobile não apresenta erros de JS', async ({page}) => {
  await page.setViewportSize({width:390,height:844});
  const errors=[]; page.on('pageerror',e=>errors.push(e.message));
  for (const path of ['/','/painel','/clientes','/veiculos','/oportunidades','/recomendacao']) {
    const r=await page.goto(path); expect(r.status()).toBe(200);
  }
  expect(errors).toEqual([]);
});
test('imagem local de fallback carrega', async ({page}) => {
  const r=await page.goto('/img/car.svg'); expect(r.status()).toBe(200);
});
