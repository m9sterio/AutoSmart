const { expect } = require('@playwright/test');
const users = {
  admin: ['admin@autosmart.com','admin123'], gerente: ['gerente@autosmart.com','gerente123'],
  vendedor: ['vendedor@autosmart.com','vendedor123'], consultor: ['consultor@autosmart.com','consultor123'],
  mecanico: ['mecanico@autosmart.com','mecanico123']
};
async function login(page, role = 'admin') {
  await page.goto('/login');
  await page.locator('[name=username]').fill(users[role][0]);
  await page.locator('[name=password]').fill(users[role][1]);
  await page.locator('button').filter({hasText: /entrar|acessar/i}).first().click();
  await expect(page).toHaveURL(/\/painel/);
}
async function go(page, path, title) {
  await page.goto(path);
  await expect(page.locator('h1')).toContainText(title);
}
function tomorrow(days = 7) { const d = new Date(); d.setDate(d.getDate() + days); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }
module.exports = { login, go, tomorrow };
