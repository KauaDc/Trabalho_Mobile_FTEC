# GUIA RÁPIDO - Adicionar Imagens de Overlay

## Passo 1: Preparar as Imagens

Crie imagens PNG com transparência para cada entidade. Você precisa de 2 versões:
- Uma para câmera **frontal** (selfie)
- Uma para câmera **traseira**

### Dicas de Design:
- Use fundo transparente (PNG com alpha channel)
- Deixe a imagem semi-transparente (70-80% de opacidade)
- Posicione o elemento assustador no centro ou em posição estratégica
- Considere a orientação da câmera:
  - **Frontal**: A pessoa estará olhando para a câmera
  - **Traseira**: Pode ter elementos ao fundo

## Passo 2: Nomear os Arquivos

Os arquivos devem seguir este padrão EXATO:

```
{id_da_entidade}_{tipo_camera}.png
```

### Exemplos:

Para a entidade "belchiorius":
- `belchiorius_frontal.png`
- `belchiorius_traseira.png`

Para a entidade "nocturna":
- `nocturna_frontal.png`
- `nocturna_traseira.png`

**IMPORTANTE**: O ID deve ser exatamente igual ao campo `id` no banco de dados!

## Passo 3: Colocar na Pasta Correta

Copie os arquivos para:
```
app/src/main/assets/overlays/
```

Estrutura final:
```
MyDemons/
└── app/
    └── src/
        └── main/
            └── assets/
                └── overlays/
                    ├── belchiorius_frontal.png
                    ├── belchiorius_traseira.png
                    ├── nocturna_frontal.png
                    ├── nocturna_traseira.png
                    ├── default_frontal.png (opcional - fallback)
                    └── default_traseira.png (opcional - fallback)
```

## Passo 4: Testar

1. Compile e instale o app
2. Faça o questionário
3. Tire uma foto (frontal ou traseira)
4. Veja o resultado com o overlay aplicado!

## Verificação

Para confirmar que os arquivos foram incluídos no APK:

```bash
# Listar assets no APK
aapt list app-debug.apk | grep overlays
```

Ou verifique os logs do app:
```bash
adb logcat | grep LocalProcessing
```

Você verá mensagens como:
```
🔍 Procurando overlay: belchiorius_frontal.png
✅ Overlay carregado: 800x1200
```

## Troubleshooting

### ❌ "Overlay não encontrado"

**Causas possíveis:**
1. Nome do arquivo errado (verifique maiúsculas/minúsculas)
2. Arquivo não está na pasta `assets/overlays/`
3. ID da entidade não coincide com o banco de dados
4. App não foi recompilado após adicionar os arquivos

**Solução:**
- Rebuild do projeto: `Build > Rebuild Project`
- Reinstale o app

### ❌ "Imagem não aparece"

**Possíveis causas:**
1. Imagem totalmente transparente (alpha = 0)
2. Imagem muito pequena
3. Formato de arquivo incorreto

**Solução:**
- Verifique a opacidade da imagem
- Use PNG com alpha channel
- Tamanho mínimo recomendado: 512x512px

### ❌ "Imagem aparece cortada"

**Causa:**
A imagem é redimensionada para caber na foto do usuário

**Solução:**
- Use proporções similares (3:4 ou 9:16)
- Deixe margem nas bordas da sua imagem

## Gerador de Placeholders (para testes)

Se você não tem imagens prontas, pode gerar placeholders:

1. No código, adicione em algum lugar (ex: botão de debug):

```kotlin
OverlayGenerator.generatePlaceholderOverlays(context)
```

2. Copie os arquivos gerados de:
```
/data/data/com.ruhan.possessao/files/overlays_generated/
```

Para:
```
app/src/main/assets/overlays/
```

3. Rebuild e reinstale

**Nota**: Placeholders são apenas para teste. Substitua por imagens reais de terror!

## Entidades Atuais no Sistema

Confira quais entidades existem:
- belchiorius
- nocturna
- (adicione mais no código se necessário)

Para cada uma você precisa criar:
- `{entidade}_frontal.png`
- `{entidade}_traseira.png`

