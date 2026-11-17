param([Parameter(Mandatory=$true)][string]$Arquivo, [switch]$ConfirmoSobrescrever)
$ErrorActionPreference = "Stop"
if (-not $ConfirmoSobrescrever) { throw "A restauração pode substituir dados! Informe -ConfirmoSobrescrever após criar outro backup." }
if (-not (Test-Path $Arquivo)) { throw "Arquivo não encontrado: $Arquivo" }
if (-not $env:DB_PASSWORD) { throw "Defina DB_PASSWORD antes de restaurar." }
if (-not (Get-Command pg_restore -ErrorAction SilentlyContinue)) { throw "Instale PostgreSQL Client Tools (pg_restore)." }
$env:PGPASSWORD = $env:DB_PASSWORD
try {
  & pg_restore -h "localhost" -p 5432 -U "postgres" -d "autosmart" --clean --if-exists --no-owner --exit-on-error $Arquivo
  if ($LASTEXITCODE -ne 0) { throw "Restauração falhou com código $LASTEXITCODE" }
  Write-Output "Restauração concluída. Verifique os registros antes de liberar acesso aos usuários."
} finally { Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue }
