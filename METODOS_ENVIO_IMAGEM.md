# 📤 Métodos de Envio de Imagens para API Gemini

## ✅ Implementado: 3 Métodos Diferentes

Agora o app suporta **3 maneiras** de enviar imagens para a API Gemini, cada uma com suas vantagens:

---

## 🎯 Método 1: File API (Padrão - Mais Eficiente)

### Como Funciona
1. Faz upload da imagem para o servidor do Google via File API
2. Recebe um URI temporário (`gs://...`)
3. Envia apenas o URI na requisição principal (muito menor!)

### Vantagens
- ✅ **Menor uso de quota** - URI é minúsculo comparado a base64
- ✅ **Mais rápido** - Requisição principal é muito menor
- ✅ **Suporta imagens grandes** - Upload separado sem limite de payload
- ✅ **Cache no servidor** - Pode reusar a mesma imagem

### Exemplo de Request
```json
{
  "contents": [{
    "parts": [
      { "text": "Coloque Belchiorius assombrando..." },
      {
        "file_data": {
          "mime_type": "image/jpeg",
          "file_uri": "gs://generativeai-uploads/abc123..."
        }
      }
    ]
  }]
}
```

### Tamanho Comparativo
- **URI**: ~100 bytes
- **vs Base64 de 35KB**: ~47.000 bytes
- **Economia**: **99.8% menor!** 🎉

---

## 🎯 Método 2: Base64 Inline (Fallback Automático)

### Como Funciona
1. Converte a imagem para base64
2. Embute o base64 direto no JSON da requisição

### Vantagens
- ✅ **Simples** - Uma única requisição
- ✅ **Funciona sempre** - Não depende de upload prévio
- ✅ **Compatível com qualquer modelo**

### Desvantagens
- ❌ Payload grande (~47KB para imagem de 35KB)
- ❌ Consome mais quota por requisição
- ❌ Mais lento (mais dados para transferir)

### Exemplo de Request
```json
{
  "contents": [{
    "parts": [
      { "text": "Coloque Belchiorius assombrando..." },
      {
        "inline_data": {
          "mime_type": "image/jpeg",
          "data": "/9j/4AAQSkZJRgABAQAA..." // ~47KB de base64
        }
      }
    ]
  }]
}
```

---

## 🎯 Método 3: Multipart Form (Alternativa)

### Como Funciona
1. Envia a imagem como multipart/form-data
2. Similar ao File API, mas estrutura diferente

### Status Atual
- Implementado mas **cai de volta para base64**
- Pode ser expandido para usar endpoints específicos

---

## 🔧 Como Trocar de Método

### No Código
Edite a variável `uploadMethod` em `AiRepository.kt`:

```kotlin
// Linha ~40
private var uploadMethod = ImageUploadMethod.FILE_API  // ← Método padrão

// Opções disponíveis:
// ImageUploadMethod.FILE_API        - Upload separado (recomendado)
// ImageUploadMethod.INLINE_BASE64   - Base64 inline (simples)
// ImageUploadMethod.MULTIPART_FORM  - Multipart (experimental)
```

### Fallback Automático
Se o File API falhar (erro 429 ou outro), o sistema **automaticamente** troca para base64:

```kotlin
if (uploadMethod == ImageUploadMethod.FILE_API && attempt < MAX_RETRIES) {
    Log.d("AiRepository", "🔄 Tentando método alternativo na próxima...")
    uploadMethod = ImageUploadMethod.INLINE_BASE64
}
```

---

## 📊 Comparação de Performance

| Métrica | File API | Base64 Inline | Multipart |
|---------|----------|---------------|-----------|
| **Tamanho do payload principal** | ~200 bytes | ~47KB | ~47KB |
| **Número de requests** | 2 (upload + generate) | 1 | 1-2 |
| **Velocidade** | ⚡⚡⚡ Rápida | ⚡⚡ Média | ⚡⚡ Média |
| **Uso de quota** | 💰 Baixo | 💰💰 Médio | 💰💰 Médio |
| **Suporte a grandes imagens** | ✅ Sim | ⚠️ Limitado | ⚠️ Limitado |
| **Simplicidade** | ⚠️ Moderada | ✅ Simples | ⚠️ Moderada |

---

## 🎬 Fluxo Completo (File API)

### 1. Upload da Imagem
```
POST https://generativelanguage.googleapis.com/upload/v1beta/files?key=API_KEY
Content-Type: multipart/form-data

[Imagem otimizada: 35KB]
```

**Resposta:**
```json
{
  "file": {
    "uri": "gs://generativeai-uploads/abc123...",
    "state": "ACTIVE",
    "sizeBytes": 35840
  }
}
```

### 2. Geração de Conteúdo
```
POST https://generativelanguage.googleapis.com/v1alpha/models/gemini-2.0.../generateContent?key=API_KEY

{
  "contents": [{
    "parts": [
      { "text": "Prompt..." },
      { "file_data": { "file_uri": "gs://..." } }
    ]
  }]
}
```

**Payload**: ~200 bytes (vs ~47.000 bytes com base64)

---

## 💡 Recomendações

### Para Produção
✅ **Use File API** (método padrão)
- Melhor performance
- Menor uso de quota
- Suporta imagens maiores

### Para Desenvolvimento/Debug
✅ **Use Base64 Inline**
- Mais simples
- Uma requisição só
- Fácil de debugar

### Se Tiver Erro 429
1. ✅ O sistema tenta File API primeiro
2. ✅ Se falhar, cai para Base64 automaticamente
3. ✅ Backoff exponencial entre tentativas

---

## 🔍 Logs para Identificar o Método

No Logcat, procure por:

```
🔧 Método: File API (upload separado)
📤 Fazendo upload via File API...
✅ Upload concluído! (234ms)
📎 File URI: gs://generativeai-uploads/...
```

ou

```
🔧 Método: Base64 inline
📊 Original: 1068KB
📊 Otimizado: 35KB (96% redução)
📊 Base64: 47KB (~48128 chars)
```

---

## 🚀 Outras Alternativas (Futuras)

### 1. Google Cloud Storage (GCS)
- Upload para seu próprio bucket
- URL pública permanente
- Controle total sobre lifecycle
- **Requer**: Configuração de GCS

### 2. URL Pública
- Se a imagem já estiver online
- Passa apenas a URL no request
- **Requer**: Servidor web para hospedar imagens

### 3. Resumable Upload (Grandes Arquivos)
- Para vídeos e imagens muito grandes
- Upload em chunks
- **Requer**: Implementação adicional

---

## 📝 Código Relevante

### Trocar Método Programaticamente
```kotlin
// No início de processImage()
AiRepository.uploadMethod = ImageUploadMethod.FILE_API

val result = AiRepository.processImage(
    context = context,
    imageUri = photoUri,
    prompt = "..."
)
```

### Forçar Base64 (sem File API)
```kotlin
// Em AiRepository.kt, linha ~40
private var uploadMethod = ImageUploadMethod.INLINE_BASE64
```

---

## ✅ Resumo

| Situação | Método Recomendado | Motivo |
|----------|-------------------|--------|
| Produção normal | File API | Mais eficiente |
| Debug/teste rápido | Base64 Inline | Mais simples |
| Erro 429 recorrente | File API | Menos quota |
| Upload grande | File API | Sem limite de payload |
| Requisição única | Base64 Inline | Menos requests |

**O app agora usa File API por padrão e cai automaticamente para Base64 se necessário!** 🎉

