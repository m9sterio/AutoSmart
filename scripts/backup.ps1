param([string]$Destino = ".\backups")
$ErrorActionPreference = "Stop"
if (-not $env:DB_PASSWORD) { throw "Configure a variável de ambiente DB_PASSWORD antes do backup." }
if (-not (Get-Command pg_dump -ErrorAction SilentlyContinue)) { throw "Instale PostgreSQL Client Tools (pg_dump)." }
New-Item -Force -ItemType Directory -Path $Destino | Out-Null
$Data = Get-Date -Format "yyyyMMdd_HHmmss"
$Arquivo = Join-Path $Destino "autosmart_$Data.dump"
$env:PGPASSWORD = $env:DB_PASSWORD
try {
  & pg_dump -h "localhost" -p 5432 -U "postgres" -d "autosmart" -Fc -f $Arquivo
  if ($LASTEXITCODE -ne 0) { throw "Backup falhou com código $LASTEXITCODE" }
  Write-Output "Backup gravado em $Arquivo. Teste restaurações periodicamente."
} finally { Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue }
