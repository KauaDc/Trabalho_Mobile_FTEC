# TESTE RÁPIDO - Gerando Overlays de Exemplo

## Como Testar o Sistema Agora

Já que o build foi bem-sucedido, você pode testar o sistema de duas formas:

### Opção 1: Usar o Gerador de Placeholders (Recomendado para teste)

1. **Adicione um botão de debug temporário** na `StartScreen.kt`:

```kotlin
// Em StartScreen.kt, adicione este botão TEMPORÁRIO:
Button(onClick = {
    // Gerar overlays de teste
    com.ruhan.possessao.ai.OverlayGenerator.generatePlaceholderOverlays(context)
    android.widget.Toast.makeText(
        context, 
        "Overlays gerados! Verifique /data/data/com.ruhan.possessao/files/overlays_generated/",
        android.widget.Toast.LENGTH_LONG
    ).show()
}) {
    Text("🛠️ Gerar Overlays de Teste")
}
```

2. **Execute o app** e clique no botão

3. **Copie os arquivos gerados**:
```bash
# Via ADB
adb pull /data/data/com.ruhan.possessao/files/overlays_generated/ ./temp_overlays/

# Depois copie para assets:
# Copie os arquivos .png de ./temp_overlays/ para app/src/main/assets/overlays/
```

4. **Rebuild** e reinstale o app

### Opção 2: Criar Overlays Manualmente (Para produção)

Se você já tem imagens prontas ou quer criar agora:

1. **Crie imagens PNG com transparência** para:
   - `belchiorius_frontal.png`
   - `belchiorius_traseira.png`
   - `nocturna_frontal.png`
   - `nocturna_traseira.png`

2. **Coloque em**: `app/src/main/assets/overlays/`

3. **Rebuild** do projeto

## Testando o Fluxo Completo

1. Abra o app
2. Passe pelas telas de questionário (sexo, idade, perguntas)
3. Clique em "Adicionar foto"
4. **Toggle entre frontal/traseira** (você verá o texto mudar)
5. Tire uma foto
6. Clique em "Gerar resultado"
7. Veja a imagem processada com o overlay!

## Verificando Logs

Durante o teste, monitore os logs:

```bash
adb logcat | grep -E "LocalProcessing|MainViewModel|CameraScreen"
```

Você deverá ver:

```
📷 Câmera: frontal (ou traseira)
🎬 Processamento LOCAL de imagem
📝 Entidade: belchiorius
🔍 Procurando overlay: belchiorius_frontal.png
✅ Overlay carregado: 800x1200
✅ Imagem salva: possessed_123456.jpg
```

## Se o Overlay Não For Encontrado

Você verá:
```
⚠️ Overlay não encontrado: belchiorius_frontal.png
   Tentando overlay genérico...
⚠️ Nenhum overlay disponível
⚠️ Aplicando efeitos básicos (sem overlay)
```

Neste caso, a foto ainda será processada com efeitos de terror básicos (escurecimento, vinheta, tons avermelhados).

## Próximos Passos Sugeridos

1. ✅ **Teste com placeholders** primeiro (Opção 1)
2. ✅ **Verifique se o sistema funciona** corretamente
3. ✅ **Substitua por imagens reais** de terror (Opção 2)
4. ✅ **Ajuste transparência/efeitos** em `LocalImageProcessor.kt` se necessário

## Ajustes Finos (Opcional)

Se quiser ajustar a intensidade dos efeitos, edite `LocalImageProcessor.kt`:

```kotlin
// Ajustar transparência do overlay
overlayPaint.alpha = 200  // 0-255 (quanto maior, mais opaco)

// Ajustar escurecimento geral
darkPaint.color = Color.argb(40, 0, 0, 0)  // Primeiro valor = opacidade
```

---

**Status Atual**: ✅ Projeto compilado com sucesso
**Próximo**: Testar no dispositivo/emulador

