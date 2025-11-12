# 🚀 GUIA RÁPIDO - Aplicar Correção Agora

## O que foi corrigido?

✅ Sistema agora usa **SOMENTE** as entidades em `sampleEntities()`  
✅ Ignorado banco de dados antigo  
✅ Apenas **"Legião"** será exibida  

## Execute estes comandos:

### 1. Desinstalar app antigo
```bash
adb uninstall com.ruhan.possessao
```

### 2. Compilar nova versão
```bash
cd c:\Users\KauaD\AndroidStudioProjects\MyDemons
.\gradlew.bat assembleDebug
```

### 3. Instalar versão corrigida
```bash
adb install app\build\outputs\apk\debug\app-debug.apk
```

### 4. Executar
```bash
adb shell am start -n com.ruhan.possessao/.app.MainActivity
```

## Ou use o script pronto:

```bash
.\reinstall_clean.bat
```

## Verificar se funcionou:

```bash
adb logcat | findstr MainViewModel
```

Deve mostrar:
```
MainViewModel: entities from sampleEntities(): 1
MainViewModel: chosen=legiao
```

✅ **1 entidade** = Funcionou!  
❌ **Mais de 1** = Execute os passos novamente

---

## Resumo da Mudança no Código

**Arquivo**: `MainViewModel.kt`

**Linha 78 (ANTES):**
```kotlin
val combined = (_entities.value + sampleEntities()).distinctBy { it.id }
```

**Linha 78 (DEPOIS):**
```kotlin
val combined = sampleEntities()
```

**Resultado**: Apenas entidades de `sampleEntities()` são usadas (atualmente só "Legião")

---

## Para adicionar mais entidades no futuro:

1. Descomente entidades em `EntityRepository.kt`
2. Desinstale o app: `adb uninstall com.ruhan.possessao`
3. Reinstale: `.\gradlew.bat assembleDebug && adb install ...`

**SEMPRE desinstale antes** para limpar banco de dados!

