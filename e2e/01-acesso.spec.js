const { test, expect } = require('@playwright/test');
const { login, go } = require('./helpers');
test('site público e tela de login abrem', async ({page}) => {
  await page.goto('/'); await expect(page.locator('body')).not.toBeEmpty();
  await page.goto('/login'); await expect(page.locator('[name=username]')).toBeVisible();
});
test('senha incorreta não permite entrar', async ({page}) => {
  await page.goto('/login'); await page.locator('[name=username]').fill('admin@autosmart.com');
  await page.locator('[name=password]').fill('senha-incorreta'); await page.locator('button').first().click();
  await expect(page).toHaveURL(/\/login/);
});
for (const role of ['admin','gerente','vendedor','consultor','mecanico']) {
  test(`login ${role} e painel`, async ({page}) => { await login(page,role); await go(page,'/painel','Painel'); });
}
test('usuário anônimo não acessa clientes', async ({page}) => {
  await page.goto('/clientes'); await expect(page).toHaveURL(/\/login/);
});
for (const role of ['gerente','vendedor','consultor','mecanico']) {
  test(`${role} não acessa administração de usuários`, async ({page}) => {
    await login(page,role); const r=await page.goto('/usuarios'); expect(r.status()).toBe(403);
  });
}
test('vendedor não pode acessar oficina', async ({page}) => {
  await login(page,'vendedor'); const r=await page.goto('/oficina'); expect(r.status()).toBe(403);
});
test('mecânico não pode acessar clientes', async ({page}) => {
  await login(page,'mecanico'); const r=await page.goto('/clientes'); expect(r.status()).toBe(403);
});
test('logout encerra sessão', async ({page}) => {
  await login(page); const form=page.locator('form[action="/logout"]');
  await expect(form).toHaveCount(1); await form.locator('button').click();
  await page.goto('/painel'); await expect(page).toHaveURL(/\/login/);
});
