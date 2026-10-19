@echo off
setlocal
set "PROJECT=%USERPROFILE%\AndroidStudioProjects\DreamCore"
echo DREAM CORE 1.4 - Instalador de archivos fuente
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
if not exist "%~dp0app\src\main\java\com\dreamcore\game\DreamShaders.kt" goto unpack
if not exist "%~dp0app\src\main\res\raw\music_chess_14.ogg" goto unpack
if not exist "%~dp0app\src\main\res\drawable-nodpi\dream_cloud_density.png" goto unpack
set "BACKUP=%PROJECT%\DreamCore_backup_1_4_%RANDOM%_%RANDOM%"
echo.
echo Creando copia de seguridad completa de app\src\main...
robocopy "%PROJECT%\app\src\main" "%BACKUP%\main" /E /COPY:DAT /R:1 /W:1 /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto backupfail
robocopy "%~dp0app\src\main\java\com\dreamcore\game" "%DEST%." /E /COPY:DAT /R:1 /W:1 /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto copyfail
robocopy "%~dp0app\src\main\res\raw" "%PROJECT%\app\src\main\res\raw" /E /COPY:DAT /R:1 /W:1 /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto copyfail
robocopy "%~dp0app\src\main\res\drawable-nodpi" "%PROJECT%\app\src\main\res\drawable-nodpi" /E /COPY:DAT /R:1 /W:1 /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto copyfail
echo.
echo Actualizacion aplicada.
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
