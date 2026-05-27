<#
fix-terminal-encoding.ps1
세션에서 PowerShell 인코딩을 UTF-8로 설정합니다. 터미널에서 한글이 깨질 때 이 스크립트를 실행하세요.
실행 방법: PowerShell에서 `.
ecipes\fix-terminal-encoding.ps1` 대신 아래처럼 실행 권장:
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\fix-terminal-encoding.ps1
#>

Write-Host "Setting console code page to UTF-8 (65001)" -ForegroundColor Cyan
chcp 65001 | Out-Null

Write-Host "Setting PowerShell output encodings to UTF-8" -ForegroundColor Cyan
$OutputEncoding = New-Object System.Text.UTF8Encoding $false
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "Done. 현재 세션에서 UTF-8 인코딩이 적용되었습니다." -ForegroundColor Green
Write-Host "영구 적용을 원하면 PowerShell 프로필에 이 스크립트 내용을 추가하세요: $PROFILE" -ForegroundColor Yellow
