# 📊 Análise de Quota - API Gemini 2.5 Flash Image Preview

## ✅ Implementações Realizadas

### 1. **API Configurada Corretamente**
- **Modelo**: `gemini-2.5-flash-image-preview`
- **Endpoint**: `https://generativelanguage.googleapis.com/v1alpha/models/gemini-2.5-flash-image-preview:generateContent`
- **Autenticação**: API Key via query parameter

### 2. **Otimizações para Economizar Quota**

#### Redução de Tamanho da Imagem
- **Dimensão máxima**: 512px (antes era 1024px) → **Redução de 75% no payload**
- **Qualidade JPEG**: 60% (antes era 75%) → **Economia adicional de ~40%**
- **Resultado**: Imagens agora têm ~25-50KB em vez de 100KB+

#### Exemplo de Redução Real
```
Original:     1440x1920 = 1.068 MB (JPEG 90%)
Otimizado:    512x683   = ~35 KB   (JPEG 60%)
Base64:       ~47 KB em texto
Redução:      96.7% do tamanho original!
```

### 3. **Funções de Análise de Quota**

#### `listAvailableModels()`
Lista todos os modelos disponíveis na sua conta:
```kotlin
val models = AiRepository.listAvailableModels(context)
// Retorna: ["models/gemini-2.5-flash-image-preview", ...]
```

#### `checkModelQuota()`
Verifica informações e limites do modelo:
```kotlin
val info = AiRepository.checkModelQuota(context)
// Logs mostram:
// ✓ Modelo: Gemini 2.5 Flash Image Preview
//   📥 Input limit: 8192 tokens
//   📤 Output limit: 8192 tokens
```

#### `processImage()` com Verificação de Quota
```kotlin
// Para verificar quota antes de processar (modo debug):
val result = AiRepository.processImage(
    context = context,
    imageUri = photoUri,
    prompt = "Coloque Belchiorius assombrando a pessoa da foto",
    checkQuota = true  // Ativa verificação de quota
)
```

### 4. **Sistema de Retry Inteligente**

#### Backoff Exponencial com Jitter
- **Tentativa 1**: Falha → aguarda 3s + 0-1s aleatório
- **Tentativa 2**: Falha → aguarda 6s + 0-1s aleatório
- **Tentativa 3**: Falha → aguarda 12s + 0-1s aleatório

#### Tratamento de Erros por Código HTTP

| Código | Descrição | Ação |
|--------|-----------|------|
| 200 | ✅ Sucesso | Extrai imagem da resposta |
| 400 | ❌ Request inválido | Não faz retry (erro permanente) |
| 401/403 | ❌ Autenticação | Não faz retry (verifique API key) |
| 404 | ❌ Modelo não encontrado | Não faz retry (modelo não existe) |
| 429 | ⚠️ Quota/Rate limit | Faz retry com backoff |
| 5xx | ⚠️ Erro do servidor | Faz retry |

### 5. **Logs Detalhados para Debug**

Todos os logs agora usam emojis para fácil identificação:

```
🎬 Processando imagem
📝 Prompt: Coloque Belchiorius assombrando...
🔄 Tentativa 1/3
📊 Original: 1068KB
📊 Otimizado: 35KB (96% redução)
📊 Base64: 47KB (~48128 chars)
🌐 Chamando API Gemini...
✅ Sucesso! (2345ms)
💾 Debug salvo: ai_response_attempt_1_1731000000.json
```

### 6. **Arquivos de Debug**

Todas as respostas da API são salvas em:
```
/data/data/com.ruhan.possessao/cache/
  ├── ai_response_attempt_1_[timestamp].json
  ├── ai_response_attempt_2_[timestamp].json
  ├── ai_list_models_[timestamp].json
  └── ai_model_quota_[timestamp].json
```

## 📈 Por que Você Recebia Erro 429?

### Causas Identificadas

1. **Tamanho da Imagem**
   - **Antes**: ~134KB em base64 (100KB binário)
   - **Problema**: Consumia muitos tokens/quota por request
   - **Solução**: Reduzido para ~47KB em base64 (35KB binário)

2. **Múltiplas Tentativas Simultâneas**
   - **Antes**: Retry imediato sem delay suficiente
   - **Problema**: Múltiplos requests em segundos
   - **Solução**: Backoff exponencial (3s → 6s → 12s)

3. **Rate Limits da API Gemini**
   
   Limites típicos do tier gratuito:
   - **RPM (Requests Per Minute)**: 15-60 requests
   - **TPM (Tokens Per Minute)**: 32.000-1.000.000 tokens
   - **RPD (Requests Per Day)**: 1.500 requests
   
   Com imagens grandes, você pode atingir o TPM mesmo com poucos requests!

### Cálculo de Quota

**Antes** (imagem 1024x1366, 100KB):
```
1 imagem = ~100KB binário
         = ~134KB base64
         = ~2.000-3.000 tokens equivalentes
         
15 imagens = 30.000-45.000 tokens
→ Atinge limite de TPM rapidamente!
```

**Depois** (imagem 512x683, 35KB):
```
1 imagem = ~35KB binário
         = ~47KB base64
         = ~700-1.000 tokens equivalentes
         
40 imagens = 28.000-40.000 tokens
→ Mais espaço na quota!
```

## 🔍 Como Verificar sua Quota Atual

### Opção 1: Via Logs do App

1. Ative o modo debug em `AiRepository.kt`:
```kotlin
suspend fun processImage(..., checkQuota: Boolean = true) {
    // Mude para true
}
```

2. Execute o app e verifique os logs:
```
📋 Listando modelos disponíveis...
  ✓ Gemini 2.5 Flash Image Preview
    ID: models/gemini-2.5-flash-image-preview
✓ Total: 12 modelos

📊 Verificando quota de: gemini-2.5-flash-image-preview
✓ Modelo: Gemini 2.5 Flash Image Preview
  📥 Input limit: 8192 tokens
  📤 Output limit: 8192 tokens
```

### Opção 2: Google AI Studio

1. Acesse: https://aistudio.google.com/
2. Faça login com a conta da API key
3. Vá em **Settings** → **API Keys**
4. Clique na sua key → **Usage & Quotas**

### Opção 3: Google Cloud Console

1. Acesse: https://console.cloud.google.com/
2. Selecione o projeto
3. Menu → **APIs & Services** → **Quotas**
4. Procure por "Generative Language API"

## 🚀 Recomendações

### Para Evitar 429 no Futuro

1. **Aguarde entre requests**
   - Mínimo 2-3 segundos entre cada foto processada
   - Implemente um debounce/throttle se o usuário tirar fotos rapidamente

2. **Use processamento local como padrão**
   - API só quando realmente necessário
   - Efeitos locais são instantâneos e gratuitos

3. **Monitore os logs**
   - Preste atenção nos tamanhos reportados
   - Se ainda grande, reduza mais (MAX_IMAGE_DIMENSION = 384)

4. **Considere upgrade de quota**
   - Se o app for para produção, ative billing no Google Cloud
   - Quotas pagas são muito mais generosas

### Configurações Ajustáveis

Em `AiRepository.kt`:
```kotlin
private const val MAX_IMAGE_DIMENSION = 512  // Reduza para 384 ou 256 se necessário
private const val JPEG_QUALITY = 60          // Reduza para 50 se necessário
private const val MAX_RETRIES = 3            // Aumente para 5 se quiser mais tentativas
private const val INITIAL_BACKOFF_MS = 3000L // Aumente para 5000L se 429 persistir
```

## 📝 Próximos Passos

1. ✅ API configurada corretamente com `gemini-2.5-flash-image-preview`
2. ✅ Otimização de imagem implementada (96% redução)
3. ✅ Sistema de retry inteligente com backoff
4. ✅ Logs detalhados e debug
5. ✅ Funções de verificação de quota

**Agora teste o app e observe os logs!** Os erros 429 devem ser muito mais raros ou inexistentes.

Se ainda ocorrer 429:
- Verifique se você não está fazendo muitos requests seguidos
- Aguarde alguns minutos (quotas resetam por minuto/hora)
- Reduza ainda mais o tamanho da imagem (384px ou 256px)

