param([Parameter(Mandatory=$true)][string]$Project)
$ErrorActionPreference = 'Stop'
$buildPath = Join-Path $Project 'app\build.gradle.kts'
if (-not (Test-Path -LiteralPath $buildPath)) { throw 'Se necesita app/build.gradle.kts. Usa el proyecto completo para otras estructuras.' }
$rawPath = Join-Path $Project 'app\src\main\res\raw'
$coverPath = Join-Path $Project 'app\src\main\res\drawable-nodpi'
for ($i = 1; $i -le 49; $i++) {
    $number = $i.ToString('D2')
    if (-not (Test-Path -LiteralPath (Join-Path $rawPath ("music_score_${number}_24.ogg")))) { throw "Falta musica $number. Restaura la copia antes de compilar." }
    if (-not (Test-Path -LiteralPath (Join-Path $coverPath ("music_cover_${number}_24.png")))) { throw "Falta miniatura $number. Restaura la copia antes de compilar." }
}
$source = [System.IO.File]::ReadAllText($buildPath)
$codePattern = '(?m)^(\s*versionCode\s*=\s*)\d+'
$namePattern = '(?m)^(\s*versionName\s*=\s*)"[^"]+"'
if ([regex]::Matches($source, $codePattern).Count -ne 1 -or [regex]::Matches($source, $namePattern).Count -ne 1) { throw 'No se encontraron las dos versiones de forma unica. No se cambio Gradle.' }
$changed = [regex]::Replace($source, $codePattern, '${1}26')
$changed = [regex]::Replace($changed, $namePattern, '${1}"2.6"')
[System.IO.File]::WriteAllText($buildPath, $changed, (New-Object System.Text.UTF8Encoding($false)))

# Delete only known superseded scores, after a complete source backup and successful copy.
$legacyList = Join-Path $PSScriptRoot 'tools\legacy_music_24.txt'
if (Test-Path -LiteralPath $legacyList) {
    foreach ($name in [System.IO.File]::ReadAllLines($legacyList)) {
        if ($name -match '^music_[a-z0-9_]+\.ogg$' -and $name -notmatch '^music_score_\d{2}_24\.ogg$') {
            $oldTrack = Join-Path $rawPath $name
            if (Test-Path -LiteralPath $oldTrack) { Remove-Item -LiteralPath $oldTrack }
        }
    }
}
