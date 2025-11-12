@echo off
REM Script para copiar overlays gerados do dispositivo para o projeto
REM Execute este script após gerar os overlays no app

echo ================================================
echo  Copiando Overlays do Dispositivo para Projeto
echo ================================================
echo.

REM Criar pasta temporária
if not exist "temp_overlays" mkdir temp_overlays

echo [1/3] Puxando overlays do dispositivo...
adb pull /data/data/com.ruhan.possessao/files/overlays_generated/ temp_overlays/
if %errorlevel% neq 0 (
    echo ERRO: Nao foi possivel conectar ao dispositivo
    echo Certifique-se de que:
    echo - O dispositivo esta conectado via USB
    echo - Depuracao USB esta ativada
    echo - O app foi executado e os overlays foram gerados
    pause
    exit /b 1
)

echo.
echo [2/3] Copiando para assets/overlays...
xcopy /Y temp_overlays\*.png app\src\main\assets\overlays\
if %errorlevel% neq 0 (
    echo ERRO: Falha ao copiar arquivos
    pause
    exit /b 1
)

echo.
echo [3/3] Limpando arquivos temporarios...
rmdir /S /Q temp_overlays

echo.
echo ================================================
echo  Sucesso! Overlays copiados para:
echo  app/src/main/assets/overlays/
echo ================================================
echo.
echo Proximos passos:
echo 1. Rebuild do projeto (gradlew assembleDebug)
echo 2. Reinstale o app
echo 3. Teste tirando fotos!
echo.
pause

