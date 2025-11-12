# Integração com Gemini 2.0 Flash Experimental

## ✅ Implementação Concluída

O aplicativo agora está configurado para usar a **API Gemini 2.0 Flash Experimental** que suporta processamento e geração de imagens.

## 📋 Configuração Atual

### Modelo Utilizado
- **Nome**: `gemini-2.0-flash-exp`
- **Versão da API**: `v1alpha`
- **Endpoint**: `https://generativelanguage.googleapis.com/v1alpha/models/gemini-2.0-flash-exp:generateContent`

### Chave de API
A chave está configurada no arquivo `AiRepository.kt`:
```kotlin
private const val API_KEY = "AIzaSyBnjW4aj1b2V3cvD_1VtN1Yqe6cTiqurIk"
```

## 🔄 Como Funciona

1. **Captura da Foto**: Usuário tira foto na tela de câmera
2. **Geração do Resultado**: Sistema identifica a entidade (demônio/espírito)
3. **Processamento de Imagem**:
   - A foto original é convertida para Base64
   - Enviada junto com o prompt: `"coloque {nome_da_entidade} assombrando a pessoa da foto"`
   - A API processa e pode retornar:
     - Uma imagem gerada (se o modelo suportar)
     - Uma descrição em texto
     - A imagem original (fallback)

4. **Exibição**: A imagem processada é exibida na tela de resultado

## 📱 Fluxo de Dados

```
Foto Original → Base64 → API Gemini 2.0 → Resposta JSON → Extração de Imagem → Exibição
```

## 🛠️ Recursos Implementados

### ✅ Funcionalidades
- ✅ Timeout configurado (60 segundos)
- ✅ Logs detalhados para debugging
- ✅ Salvamento de respostas em arquivos JSON para análise
- ✅ Fallback para imagem original em caso de erro
- ✅ Suporte para múltiplos formatos (file://, content://, http://)
- ✅ Extração automática de imagens em Base64 da resposta

### 📊 Logs e Debug
Todas as respostas da API são salvas em:
```
/data/user/0/com.ruhan.possessao/cache/ai_response_gemini2_[timestamp].json
```

## ⚠️ Observações Importantes

### Modelo Experimental
O modelo `gemini-2.0-flash-exp` está em fase experimental. Isso significa:
- ✅ **Gratuito** durante o período de teste
- ⚠️ Pode ter **limitações** de uso
- ⚠️ Pode **não estar disponível** permanentemente
- ⚠️ A geração de imagens pode **não funcionar como esperado**

### Alternativas se Não Funcionar

Se o modelo não suportar geração de imagens, você pode:

1. **Usar APIs Pagas de Geração de Imagens**:
   - Stability AI (Stable Diffusion) - ~$0.002/imagem
   - OpenAI DALL-E 3 - ~$0.04-0.12/imagem
   - Replicate - Preços variados

2. **Processar Localmente**:
   - Adicionar filtros e efeitos na foto original
   - Sobrepor imagens de terror pré-definidas
   - Aplicar máscaras e distorções

## 🔍 Testando

Para testar a integração:

1. Execute o app
2. Responda o questionário
3. Tire uma foto
4. Gere o resultado
5. Verifique os logs no Logcat:
   ```
   Tag: AiRepository
   ```

6. Verifique os arquivos de resposta no cache do dispositivo

## 📝 Próximos Passos

Se a API não retornar imagens geradas:
1. Verifique os logs e arquivos JSON salvos
2. Analise a estrutura da resposta da API
3. Ajuste o código de extração conforme necessário
4. Considere implementar processamento local como fallback

## 💡 Dicas

- A API pode levar alguns segundos para responder
- Mantenha as fotos em resolução moderada (não muito grandes)
- Monitore os logs para entender o comportamento da API
- A chave de API tem limites de uso gratuito

