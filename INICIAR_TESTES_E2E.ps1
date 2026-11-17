param([switch]$Visivel)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$env:AUTOSMART_E2E_DB = 'autosmart_e2e'
$env:AUTOSMART_DEMO_ENABLED = 'true'
$env:DB_URL = 'jdbc:postgresql://localhost:5432/autosmart_e2e'
Write-Host 'ATENCAO: testes criam dados no banco autosmart_e2e, NUNCA no autosmart principal.' -ForegroundColor Yellow
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) { throw 'Instale Node.js LTS (inclui npm) antes de executar.' }
if (-not (Test-Path node_modules)) { npm install; if ($LASTEXITCODE -ne 0) { throw 'Falha no npm install' } }
if (-not (Test-Path "$env:LOCALAPPDATA\ms-playwright")) { npx playwright install chromium; if ($LASTEXITCODE -ne 0) { throw 'Falha na instalacao do Chromium' } }
try {
  $r = Invoke-WebRequest 'http://127.0.0.1:8092/actuator/health' -UseBasicParsing -TimeoutSec 2
  if ($r.StatusCode -eq 200) { throw 'Porta 8092 ocupada. Pare o processo antes para garantir que os testes usem o banco isolado.' }
} catch [System.Net.WebException] { }
Write-Host 'Iniciando AutoSmart na porta 8092 com banco de testes...' -ForegroundColor Cyan
$server = Start-Process -FilePath 'mvn.cmd' -ArgumentList 'spring-boot:run','-Dspring-boot.run.arguments=--server.port=8092' -PassThru -NoNewWindow
try {
  $ready = $false
  for ($i=0; $i -lt 90; $i++) {
    Start-Sleep -Seconds 2
    try { $r=Invoke-WebRequest 'http://127.0.0.1:8092/actuator/health' -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -eq 200) { $ready=$true; break } } catch { }
    if ($server.HasExited) { throw 'O Spring Boot encerrou antes de iniciar. Verifique o log acima.' }
  }
  if (-not $ready) { throw 'Servidor não iniciou em 180 segundos.' }
  if ($Visivel) { npx playwright test --headed --workers=1 } else { npx playwright test --workers=1 }
  Write-Host 'Relatorio: playwright-report\index.html' -ForegroundColor Green
} finally {
  if (-not $server.HasExited) { Stop-Process -Id $server.Id -Force -ErrorAction SilentlyContinue }
}
