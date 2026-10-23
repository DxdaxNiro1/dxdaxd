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
for ($i = 50; $i -le 52; $i++) {
    foreach ($pair in @(@($rawPath,"music_score_${i}_27.ogg"),@($coverPath,"music_cover_${i}_27.png"))) {
        if (-not (Test-Path -LiteralPath (Join-Path $pair[0] $pair[1]))) { throw "Falta contenido Vigilia: $($pair[1])." }
    }
}
for ($i = 53; $i -le 55; $i++) {
    foreach ($pair in @(@($rawPath,"music_score_${i}_30.ogg"),@($coverPath,"music_cover_${i}_30.png"))) {
        if (-not (Test-Path -LiteralPath (Join-Path $pair[0] $pair[1]))) { throw "Falta contenido El Ultimo Tren: $($pair[1])." }
    }
}
for ($i = 56; $i -le 62; $i++) {
    foreach ($pair in @(@($rawPath,"music_score_${i}_32.ogg"),@($coverPath,"music_cover_${i}_32.png"))) {
        if (-not (Test-Path -LiteralPath (Join-Path $pair[0] $pair[1]))) { throw "Falta contenido Terror: $($pair[1])." }
    }
}
$source = [System.IO.File]::ReadAllText($buildPath)
$codePattern = '(?m)^(\s*versionCode\s*=\s*)\d+'
$namePattern = '(?m)^(\s*versionName\s*=\s*)"[^"]+"'
if ([regex]::Matches($source, $codePattern).Count -ne 1 -or [regex]::Matches($source, $namePattern).Count -ne 1) { throw 'No se encontraron las dos versiones de forma unica. No se cambio Gradle.' }
$minimumPattern = '(?m)^(\s*minSdk\s*=\s*)\d+'
if ([regex]::Matches($source, $minimumPattern).Count -ne 1) { throw 'No se encontro minSdk de forma unica. No se cambio Gradle.' }
$manifestPath = Join-Path $Project 'app\src\main\AndroidManifest.xml'
$manifest = [System.IO.File]::ReadAllText($manifestPath)
if ($manifest -notmatch '@android:style/Theme.Material.NoActionBar|@style/DreamTheme29') { throw 'Tema personalizado: aplica los cambios manuales de LEEME.md antes de cambiar minSdk.' }
$manifest = $manifest.Replace('@android:style/Theme.Material.NoActionBar', '@style/DreamTheme29')
$changed = [regex]::Replace($source, $codePattern, '${1}32')
$changed = [regex]::Replace($changed, $namePattern, '${1}"3.1"')
$changed = [regex]::Replace($changed, $minimumPattern, '${1}19')
[System.IO.File]::WriteAllText($manifestPath, $manifest, (New-Object System.Text.UTF8Encoding($false)))
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
