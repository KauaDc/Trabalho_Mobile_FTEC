# Sistema de Sobreposição de Imagens - MyDemons

## Visão Geral

O aplicativo agora usa um sistema de **sobreposição de imagens pré-carregadas** ao invés de APIs externas para gerar o efeito de "possessão" nas fotos dos usuários.

## Como Funciona

### 1. Captura de Foto
- Usuário tira foto com câmera **frontal** ou **traseira**
- O tipo de câmera é salvo automaticamente
- A foto é armazenada no cache do dispositivo

### 2. Processamento
Quando o resultado é gerado:
1. Sistema identifica a entidade com maior probabilidade (ex: "belchiorius")
2. Carrega imagem de overlay correspondente: `{entidade}_{tipo_camera}.png`
3. Sobrepõe a imagem da entidade na foto do usuário
4. Aplica efeitos de atmosfera de terror
5. Retorna imagem final processada

### 3. Estrutura de Arquivos

```
app/src/main/assets/overlays/
├── README.md
├── belchiorius_frontal.png    # Overlay para Belchiorius (câmera frontal)
├── belchiorius_traseira.png   # Overlay para Belchiorius (câmera traseira)
├── nocturna_frontal.png        # Overlay para Nocturna (câmera frontal)
├── nocturna_traseira.png       # Overlay para Nocturna (câmera traseira)
├── default_frontal.png         # Fallback genérico (frontal)
└── default_traseira.png        # Fallback genérico (traseira)
```

## Fluxo de Decisão

```
1. Usuário escolhe tipo de câmera → [Frontal] ou [Traseira]
2. Tira foto → URI salvo + tipo de câmera
3. Sistema avalia respostas → Determina entidade (ex: "nocturna")
4. Busca overlay → "nocturna_frontal.png" (se câmera frontal)
5. Se não encontrar → Tenta "default_frontal.png"
6. Se ainda não encontrar → Aplica apenas efeitos básicos de terror
7. Retorna imagem processada
```

## Customização

### Adicionar Nova Entidade

1. Crie as imagens PNG com transparência:
   - `{nova_entidade}_frontal.png`
   - `{nova_entidade}_traseira.png`

2. Coloque na pasta: `app/src/main/assets/overlays/`

3. A entidade já cadastrada no banco de dados será automaticamente reconhecida

### Formato das Imagens

- **Formato**: PNG com canal alpha (transparência)
- **Tamanho**: Livre (será redimensionado automaticamente)
- **Transparência**: Recomendado 70-80% de opacidade para efeito de "fantasma"
- **Conteúdo**: Figura da entidade/demônio em posição central

### Exemplo de Criação

Para criar overlays placeholder durante desenvolvimento:

```kotlin
// No onCreate() ou em botão de debug:
OverlayGenerator.generatePlaceholderOverlays(context)
// Copie os arquivos gerados de filesDir/overlays_generated para assets/overlays/
```

## Vantagens desta Abordagem

✅ **Sem dependência de APIs externas** - Funciona offline
✅ **Sem limites de quota** - Processamento local ilimitado
✅ **Controle total** - Você define exatamente como cada entidade aparece
✅ **Performance** - Processamento instantâneo
✅ **Personalização** - Diferentes overlays para câmera frontal/traseira
✅ **Privacidade** - Fotos não são enviadas para servidores

## Melhorias Futuras

- [ ] Adicionar múltiplas variações por entidade (ex: belchiorius_frontal_1.png, belchiorius_frontal_2.png)
- [ ] Sistema de animação (overlay animado)
- [ ] Efeitos de partículas adicionais (névoa, sombras)
- [ ] Editor in-app para ajustar posição/tamanho do overlay
- [ ] Galeria de overlays para o usuário escolher manualmente

## Troubleshooting

**Overlay não aparece?**
1. Verifique se o arquivo existe em `assets/overlays/`
2. Verifique o nome do arquivo (deve seguir padrão exato)
3. Veja os logs: `adb logcat | grep LocalProcessing`

**Imagem fica muito escura?**
- Ajuste o alpha do `darkPaint` em `combineImages()` (LocalImageProcessor.kt)

**Overlay muito opaco/transparente?**
- Ajuste o `alpha` do `overlayPaint` em `combineImages()` (atualmente 200/255)

