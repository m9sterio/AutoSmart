const { defineConfig } = require('@playwright/test');
if (process.env.AUTOSMART_E2E_DB !== 'autosmart_e2e') {
  throw new Error('Proteção: defina AUTOSMART_E2E_DB=autosmart_e2e e use somente o banco de testes isolado.');
}
module.exports = defineConfig({
  testDir: './e2e', timeout: 30000, expect: { timeout: 7000 },
  fullyParallel: false, workers: 1, retries: 0,
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'playwright-report' }], ['junit', { outputFile: 'test-results/e2e-results.xml' }]],
  use: { baseURL: 'http://127.0.0.1:8092', trace: 'retain-on-failure', screenshot: 'only-on-failure', video: 'retain-on-failure', actionTimeout: 10000 },
  projects: [{ name: 'chromium', use: { browserName: 'chromium', viewport: { width: 1440, height: 900 } } }]
});
