// Instale k6: https://grafana.com/docs/k6/latest/set-up/install-k6/
// Execute com TEST_USER, TEST_PASSWORD e BASE_URL; use só em ambiente de testes.
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
export const options = {
  stages: [{ duration: '30s', target: 50 }, { duration: '2m', target: 50 }, { duration: '15s', target: 0 }],
  thresholds: { http_req_duration: ['p(95)<3000'], http_req_failed: ['rate<0.01'] },
};
const base = __ENV.BASE_URL || 'http://localhost:8080';
const username = __ENV.TEST_USER || '';
const password = __ENV.TEST_PASSWORD || '';
export default function () {
  if (!username || !password) throw Error('Informe TEST_USER e TEST_PASSWORD.');
  const loginPage = http.get(base + '/login');
  const csrf = loginPage.body.match(/name="_csrf"[^>]*value="([^"]+)"/) ||
    loginPage.body.match(/value="([^"]+)"[^>]*name="_csrf"/);
  if (!csrf) throw Error('CSRF não encontrado na página de login.');
  const result = http.post(base + '/login', { username, password, _csrf: csrf[1] });
  check(result, { 'autenticado': r => r.status === 200 && r.url.indexOf('painel') >= 0 });
  for (const rota of ['/painel', '/clientes', '/veiculos', '/oportunidades', '/recomendacao', '/propostas']) {
    const resposta = http.get(base + rota);
    check(resposta, { [`${rota} retorna 200`]: r => r.status === 200 });
    sleep(0.5);
  }
}
