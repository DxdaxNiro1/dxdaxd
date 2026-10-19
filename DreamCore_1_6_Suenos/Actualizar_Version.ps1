param([Parameter(Mandatory=$true)][string]$Project)
$ErrorActionPreference = 'Stop'
$buildPath = Join-Path $Project 'app\build.gradle.kts'
if (-not (Test-Path -LiteralPath $buildPath)) { throw 'Se necesita app/build.gradle.kts. Usa el proyecto completo para otras estructuras.' }
$source = [System.IO.File]::ReadAllText($buildPath)
$codePattern = '(?m)^(\s*versionCode\s*=\s*)\d+'
$namePattern = '(?m)^(\s*versionName\s*=\s*)"[^"]+"'
if ([regex]::Matches($source, $codePattern).Count -ne 1 -or [regex]::Matches($source, $namePattern).Count -ne 1) { throw 'No se encontraron las dos versiones de forma unica. No se cambio Gradle.' }
$changed = [regex]::Replace($source, $codePattern, '${1}16')
$changed = [regex]::Replace($changed, $namePattern, '${1}"1.6"')
[System.IO.File]::WriteAllText($buildPath, $changed, (New-Object System.Text.UTF8Encoding($false)))
