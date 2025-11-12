@echo off
echo ================================================
echo  Limpando dados antigos e reinstalando app
echo ================================================
echo.

echo [1/4] Desinstalando versao antiga...
adb uninstall com.ruhan.possessao
if %errorlevel% neq 0 (
    echo Aviso: App nao estava instalado ou falha ao desinstalar
)

echo.
echo [2/4] Compilando nova versao...
cd /d "%~dp0"
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo ERRO: Falha ao compilar
    pause
    exit /b 1
)

echo.
echo [3/4] Instalando nova versao...
adb install app\build\outputs\apk\debug\app-debug.apk
if %errorlevel% neq 0 (
    echo ERRO: Falha ao instalar
    pause
    exit /b 1
)

echo.
echo [4/4] Iniciando app...
adb shell am start -n com.ruhan.possessao/.app.MainActivity

echo.
echo ================================================
echo  Sucesso! App reinstalado com banco de dados limpo
echo ================================================
echo.
echo Agora o app usara apenas as entidades definidas em sampleEntities()
echo (Atualmente apenas: Legiao)
echo.
pause

