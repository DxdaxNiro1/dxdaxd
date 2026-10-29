@echo off
setlocal
set "PROJECT=%USERPROFILE%\AndroidStudioProjects\DreamCore"
echo DREAM CORE 3.5 - Instalador de archivos fuente
echo Cierra el juego antes de continuar.
echo.
echo Carpeta sugerida: %PROJECT%
set /p "PROJECT=Ruta de tu proyecto (Enter para usar la sugerida): "
set "PROJECT=%PROJECT:"=%"
if not exist "%PROJECT%\app\src\main\AndroidManifest.xml" goto invalid
set /a MATCHES=0
for /r "%PROJECT%\app\src\main" %%F in (DreamWorlds.kt) do if exist "%%~fF" (
    set /a MATCHES+=1
    set "DEST=%%~dpF"
)
if not "%MATCHES%"=="1" goto ambiguous
if not exist "%DEST%DreamRenderer.kt" goto invalid
if not exist "%DEST%MainActivity.kt" goto invalid
if not exist "%PROJECT%\app\build.gradle.kts" goto invalid
if not exist "%~dp0app\src\main\java\com\dreamcore\game\DreamShaders.kt" goto unpack
if not exist "%~dp0app\src\main\res\raw\fx_stone_0_17.ogg" goto unpack
if not exist "%~dp0app\src\main\res\drawable-nodpi\dream_moon_face.png" goto unpack
if not exist "%~dp0app\src\main\res\raw\music_score_63_33.ogg" goto unpack
if not exist "%~dp0app\src\main\res\drawable-nodpi\music_cover_63_33.png" goto unpack
if not exist "%~dp0app\src\main\java\com\dreamcore\game\DreamChapter33.kt" goto unpack
if not exist "%~dp0app\src\main\res\raw\music_score_64_34.ogg" goto unpack
if not exist "%~dp0app\src\main\res\drawable-nodpi\music_cover_64_34.png" goto unpack
if not exist "%~dp0app\src\main\java\com\dreamcore\game\DreamCatalogue34.kt" goto unpack
if not exist "%~dp0app\src\main\res\drawable-nodpi\dream_entities_22.png" goto unpack
if not exist "%~dp0app\src\main\res\raw\music_score_49_24.ogg" goto unpack
if not exist "%~dp0app\src\main\res\drawable-nodpi\dream_botany_26.png" goto unpack
if not exist "%~dp0app\src\main\res\drawable-nodpi\dream_cloud_volume_26.png" goto unpack
if not exist "%~dp0app\src\main\res\drawable-nodpi\dream_lunar_faces_27.png" goto unpack
if not exist "%~dp0app\src\main\res\drawable-nodpi\dream_guardians_27.png" goto unpack
if not exist "%~dp0app\src\main\res\raw\music_score_52_27.ogg" goto unpack
if not exist "%~dp0app\src\main\java\com\dreamcore\game\DreamIndexed28.kt" goto unpack
if not exist "%~dp0app\src\main\java\com\dreamcore\game\DreamPrograms28.kt" goto unpack
if not exist "%~dp0app\src\main\java\com\dreamcore\game\DreamPixels301.kt" goto unpack
if not exist "%~dp0app\src\main\java\com\dreamcore\game\DreamPlay30.kt" goto unpack
if not exist "%~dp0app\src\main\res\raw\music_score_55_30.ogg" goto unpack
if not exist "%~dp0app\src\main\java\com\dreamcore\game\DreamGl29.kt" goto unpack
if not exist "%~dp0app\src\main\java\com\dreamcore\game\DreamTexture29.kt" goto unpack
if not exist "%~dp0app\src\main\res\values\dream_theme_29.xml" goto unpack
if not exist "%~dp0app\src\main\res\values-v21\dream_theme_29.xml" goto unpack
if not exist "%~dp0app\src\main\res\drawable-nodpi\dream_entities_32.png" goto unpack
if not exist "%~dp0app\src\main\res\raw\music_score_62_32.ogg" goto unpack
set "BACKUP=%PROJECT%\DreamCore_backup_3_1_%RANDOM%_%RANDOM%"
echo.
echo Creando copia de seguridad completa de app\src\main...
robocopy "%PROJECT%\app\src\main" "%BACKUP%\main" /E /COPY:DAT /R:1 /W:1 /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto backupfail
copy /y "%PROJECT%\app\build.gradle.kts" "%BACKUP%\build.gradle.kts" >nul
if errorlevel 1 goto backupfail
robocopy "%~dp0app\src\main\java\com\dreamcore\game" "%DEST%." /E /COPY:DAT /R:1 /W:1 /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto copyfail
robocopy "%~dp0app\src\main\res\raw" "%PROJECT%\app\src\main\res\raw" /E /COPY:DAT /R:1 /W:1 /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto copyfail
robocopy "%~dp0app\src\main\res\drawable-nodpi" "%PROJECT%\app\src\main\res\drawable-nodpi" /E /COPY:DAT /R:1 /W:1 /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto copyfail
robocopy "%~dp0app\src\main\res\values" "%PROJECT%\app\src\main\res\values" dream_theme_29.xml /COPY:DAT /R:1 /W:1 /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto copyfail
robocopy "%~dp0app\src\main\res\values-v21" "%PROJECT%\app\src\main\res\values-v21" dream_theme_29.xml /COPY:DAT /R:1 /W:1 /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto copyfail
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Actualizar_Version.ps1" -Project "%PROJECT%"
if errorlevel 1 goto copyfail
echo Actualizacion 3.2 aplicada.
echo Copia de seguridad: %BACKUP%\main
echo Abre Android Studio y ejecuta gradlew.bat assembleDebug.
echo Si quieres volver atras, lee LEEME.md antes de restaurar la copia.
pause
exit /b 0
:invalid
echo No se encontro el proyecto esperado. No se modifico.
pause
exit /b 1
:ambiguous
echo Hay cero o varios DreamWorlds.kt. No se modifico el proyecto.
echo Sigue la instalacion manual de LEEME.md.
pause
exit /b 1
:unpack
echo Extrae TODO el ZIP antes de abrir el instalador.
pause
exit /b 1
:backupfail
echo No se pudo completar la copia de seguridad. Proyecto sin actualizar.
pause
exit /b 1
:copyfail
echo La copia fallo. No compiles aun; consulta LEEME.md para restaurar.
echo Copia de seguridad: %BACKUP%\main
pause
exit /b 1
