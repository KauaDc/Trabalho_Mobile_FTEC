# 🔧 PROBLEMA RESOLVIDO - Múltiplas Entidades Aparecendo

## Problema Identificado

Mesmo deixando apenas "Legião" descomentada no `sampleEntities()`, outras entidades ainda apareciam nos resultados.

## Causa Raiz

O código em `MainViewModel.kt` estava **combinando** duas fontes de entidades:

```kotlin
// ANTES (linha 83)
val combined = (_entities.value + sampleEntities()).distinctBy { it.id }
```

Isso fazia com que:
1. **Entidades antigas** salvas no banco de dados permanecessem
2. **Entidades de sampleEntities()** fossem adicionadas
3. Resultado: Todas as entidades antigas + novas apareciam

## Solução Implementada

### 1. ✅ Uso Apenas de sampleEntities()

Modifiquei o código para usar **SOMENTE** as entidades definidas em `sampleEntities()`:

```kotlin
// DEPOIS (MainViewModel.kt, linha 78)
val combined = sampleEntities()
```

Agora o sistema ignora o banco de dados antigo e usa apenas as entidades que você definir.

### 2. ✅ Comentado Código de Múltiplas Entidades

Removi a linha que forçava ter pelo menos 3 entidades:

```kotlin
// ANTES
repo.replaceWithSampleIfBelow(3)  // Forçava 3+ entidades

// DEPOIS
// repo.replaceWithSampleIfBelow(3)  // Comentado
```

## Como Aplicar a Correção

### Opção A: Script Automático (Recomendado)

Execute o script que criei:

```bash
reinstall_clean.bat
```

Ele vai:
1. Desinstalar o app (limpa banco de dados)
2. Compilar nova versão
3. Instalar app limpo
4. Iniciar automaticamente

### Opção B: Manual

1. **Desinstalar o app atual** (para limpar banco de dados):
   ```bash
   adb uninstall com.ruhan.possessao
   ```

2. **Compilar e instalar nova versão**:
   ```bash
   gradlew.bat assembleDebug
   adb install app\build\outputs\apk\debug\app-debug.apk
   ```

3. **Executar o app**

## Verificação

Após reinstalar, verifique os logs:

```bash
adb logcat | grep MainViewModel
```

Você deverá ver:

```
MainViewModel: entities from sampleEntities(): 1
MainViewModel: chosen=legiao(XX%)
```

Se aparecer `entities from sampleEntities(): 1`, significa que está funcionando corretamente!

## Status Atual de Entidades

No arquivo `EntityRepository.kt`, apenas **1 entidade** está ativa:

- ✅ **Legião** (Cristã)
- ❌ Pazuzu (comentada)
- ❌ Lamashtu (comentada)
- ❌ Beelzebub (comentada)
- ❌ Aka Oni (comentada)
- ❌ Ao Oni (comentada)
- ❌ Namahage (comentada)
- ❌ Ifrit (comentada)
- ❌ Marid (comentada)
- ❌ Ghul (comentada)
- ❌ Si'lat (comentada)

## Como Adicionar Mais Entidades

Para adicionar mais entidades no futuro:

1. Descomente a entidade desejada em `EntityRepository.kt`
2. **Desinstale e reinstale o app** (para limpar cache)
3. Teste

**IMPORTANTE**: Sempre desinstale antes de reinstalar para garantir que o banco de dados seja limpo.

## Overlay Correspondente

Lembre-se de criar os overlays para "Legião":

```
app/src/main/assets/overlays/
├── legiao_frontal.png
└── legiao_traseira.png
```

Se não tiver overlays, o app usará efeitos básicos de terror (funcionará normalmente).

## Arquivos Modificados

- ✏️ `MainViewModel.kt` - Removida combinação com banco de dados
- ➕ `reinstall_clean.bat` - Script para reinstalar com banco limpo

## Resumo da Correção

| Antes | Depois |
|-------|--------|
| Usava DB + sampleEntities() | Usa apenas sampleEntities() |
| Entidades antigas permaneciam | Apenas entidades ativas são usadas |
| Múltiplas entidades apareciam | Apenas "Legião" aparece |

---

**Status**: ✅ Problema corrigido  
**Ação necessária**: Desinstalar e reinstalar o app  
**Script pronto**: `reinstall_clean.bat`

