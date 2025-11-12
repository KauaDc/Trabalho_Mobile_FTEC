# 🎯 GUIAS DE CENTRALIZAÇÃO ADICIONADAS

## ✅ Nova Funcionalidade Implementada

Adicionei **guias visuais de centralização** na tela da câmera para ajudar o usuário a posicionar corretamente o rosto e o corpo!

## 📸 Como Funcionam as Guias

### Câmera Frontal (Selfie) - Guia para Rosto

```
┌──────────────────────────────┐
│   "Centralize seu rosto"     │
│                              │
│        ╭─────────╮           │
│       ╱           ╲          │
│      │      +      │   ← Oval tracejada
│      │   ───┼───   │   ← Linha de olhos
│      │      │      │   ← Linha vertical (nariz)
│       ╲           ╱          │
│        ╰─────────╯           │
│                              │
│      [Botão Capturar]        │
└──────────────────────────────┘
```

**Características:**
- ✅ Oval tracejada branca (55% da largura, 35% da altura)
- ✅ Linha horizontal para alinhar olhos
- ✅ Linha vertical para centralizar nariz
- ✅ Texto: "Centralize seu rosto"

### Câmera Traseira - Guia para Corpo Inteiro

```
┌──────────────────────────────┐
│   "Centralize o corpo"       │
│                              │
│    ┏━━━━━━━━━━━━┓            │
│    ┃     ───    ┃  ← Cabeça  │
│    ┃      │     ┃            │
│    ┃      │     ┃            │
│    ┃   ───┼───  ┃  ← Cintura │
│    ┃      │     ┃            │
│    ┃      │     ┃            │
│    ┗━━━━━━━━━━━━┛            │
│                              │
│      [Botão Capturar]        │
└──────────────────────────────┘
```

**Características:**
- ✅ Retângulo tracejado branco (60% da largura, 75% da altura)
- ✅ Linha horizontal superior (12% de altura - cabeça)
- ✅ Linha horizontal central (55% de altura - cintura)
- ✅ Linha vertical central (simetria)
- ✅ Cantos reforçados para melhor visibilidade
- ✅ Texto: "Centralize o corpo"

## 🎨 Detalhes Visuais

### Cores e Estilo
- **Cor**: Branco semi-transparente (70% opacidade)
- **Estilo**: Linhas tracejadas (dash pattern)
- **Espessura**: 3px para linhas principais, 1.5px para guias
- **Texto**: Branco com sombra preta para legibilidade

### Responsividade
- ✅ Adapta-se automaticamente ao tamanho da tela
- ✅ Proporções relativas (não valores fixos)
- ✅ Centralização automática

## 🔄 Toggle Automático

As guias mudam automaticamente quando você alterna entre câmeras:

```
[Câmera Traseira] → Clica toggle → [Câmera Frontal]
   (Guia corpo)                      (Guia rosto)
```

## 📱 Experiência do Usuário

### Fluxo Completo:

1. **Abrir câmera**
   - Aparecem guias de centralização

2. **Escolher tipo** (toggle frontal/traseira)
   - Guias mudam automaticamente

3. **Posicionar-se**
   - Seguir as linhas tracejadas
   - Alinhar com as guias

4. **Capturar**
   - Foto centralizada perfeita!

5. **Ver resultado**
   - Overlay aplicado corretamente

## 🎯 Benefícios

✅ **Melhor enquadramento** - Usuário sabe onde posicionar
✅ **Consistência** - Todas as fotos bem centralizadas
✅ **Overlay preciso** - Sobreposição alinhada corretamente
✅ **Profissional** - Aparência mais cuidada
✅ **Intuitivo** - Visual claro e fácil de entender

## 🔧 Detalhes Técnicos

### Componente Criado:
```kotlin
@Composable
private fun CentralizationGuide(useFrontCamera: Boolean)
```

### Implementação:
- Canvas do Jetpack Compose
- Desenho vetorial (não imagens)
- Performance otimizada
- Sem impacto na captura

### Arquivos Modificados:
- ✏️ `CameraScreen.kt` - Adicionado componente de guias

## 📊 Dimensões das Guias

### Câmera Frontal (Rosto):
- Largura oval: 55% da tela
- Altura oval: 35% da tela
- Linha olhos: 10% acima do centro
- Centralizado vertical e horizontalmente

### Câmera Traseira (Corpo):
- Largura retângulo: 60% da tela
- Altura retângulo: 75% da tela
- Linha cabeça: 12% do topo
- Linha cintura: 55% do topo
- Centralizado vertical e horizontalmente

## 🎨 Customização Futura

Se quiser ajustar as guias:

```kotlin
// Em CentralizationGuide()

// Tamanho do oval (frontal)
val ovalWidth = canvasWidth * 0.55f  // Ajuste aqui (0.0-1.0)
val ovalHeight = canvasHeight * 0.35f

// Tamanho do retângulo (traseira)
val rectWidth = canvasWidth * 0.6f   // Ajuste aqui
val rectHeight = canvasHeight * 0.75f

// Cor e opacidade
val guideColor = Color.White.copy(alpha = 0.7f)  // 0.0-1.0

// Espessura das linhas
val strokeWidth = 3f  // pixels
```

## ✅ Status

- [x] Guia de rosto (câmera frontal)
- [x] Guia de corpo (câmera traseira)
- [x] Toggle automático
- [x] Texto instrucional
- [x] Linhas de alinhamento
- [x] Cantos reforçados (traseira)
- [x] Responsivo a tamanhos de tela
- [⏳] Build em andamento...

## 🚀 Teste Agora!

1. **Aguarde compilação** terminar
2. **Reinstale o app**:
   ```bash
   adb uninstall com.ruhan.possessao
   adb install app\build\outputs\apk\debug\app-debug.apk
   ```
3. **Abra a câmera** no app
4. **Veja as guias** aparecendo
5. **Toggle frontal/traseira** para ver diferentes guias
6. **Tire uma foto** bem centralizada!

---

**🎊 Guias de centralização prontas!**

Agora seus usuários terão uma experiência muito melhor ao tirar fotos! 📸✨

