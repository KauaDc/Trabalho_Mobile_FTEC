package com.ruhan.possessao.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Processamento LOCAL de imagens
 * SEM dependências externas (sem Firebase, sem Google AI, sem APIs)
 * Sobrepõe imagens pré-carregadas da entidade sobre a foto do usuário
 */
object LocalImageProcessor {

    /**
     * Processa imagem sobrepondo a imagem da entidade
     * @param context Contexto Android
     * @param imageUri URI da foto tirada pelo usuário
     * @param entityId ID da entidade (ex: "belchiorius")
     * @param cameraType Tipo de câmera ("frontal" ou "traseira")
     */
    suspend fun processImage(
        context: Context,
        imageUri: String,
        entityId: String,
        cameraType: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("LocalProcessing", "🎬 Processamento LOCAL de imagem")
                Log.d("LocalProcessing", "📝 Entidade: $entityId")
                Log.d("LocalProcessing", "📷 Câmera: $cameraType")
                Log.d("LocalProcessing", "")
                Log.d("LocalProcessing", "╔════════════════════════════════════════════╗")
                Log.d("LocalProcessing", "║  🎨 SOBREPOSIÇÃO DE IMAGENS               ║")
                Log.d("LocalProcessing", "║  Imagem pré-carregada sobre foto          ║")
                Log.d("LocalProcessing", "╚════════════════════════════════════════════╝")
                Log.d("LocalProcessing", "")

                // Processar imagem sobrepondo a entidade
                val result = overlayEntityImage(context, imageUri, entityId, cameraType)

                Log.d("LocalProcessing", "✅ Processamento concluído!")
                return@withContext result

            } catch (e: Exception) {
                Log.e("LocalProcessing", "❌ Erro: ${e.message}", e)
                return@withContext imageUri // Retorna original se falhar
            }
        }
    }


    private fun loadBitmap(context: Context, imageUri: String): Bitmap? {
        return try {
            when {
                imageUri.startsWith("file://") -> {
                    BitmapFactory.decodeFile(imageUri.substring(7))
                }
                imageUri.startsWith("content://") -> {
                    context.contentResolver.openInputStream(imageUri.toUri())?.use {
                        BitmapFactory.decodeStream(it)
                    }
                }
                else -> {
                    BitmapFactory.decodeFile(imageUri)
                }
            }
        } catch (e: Exception) {
            Log.e("LocalProcessing", "Erro ao carregar bitmap: ${e.message}")
            null
        }
    }

    /**
     * Carrega imagem da entidade dos assets
     * Tenta múltiplos formatos de nome:
     * 1. {entityId}_{cameraType}.png (ex: legiao_frontal.png)
     * 2. {entityId}{cameraType}.png (ex: legiaofrontal.png)
     * 3. default_{cameraType}.png (fallback genérico)
     */
    private fun loadEntityOverlay(context: Context, entityId: String, cameraType: String): Bitmap? {
        // Lista de possíveis nomes de arquivo (ordem de prioridade)
        val possibleNames = listOf(
            "${entityId}_${cameraType}.png",      // legiao_frontal.png
            "${entityId}${cameraType}.png",        // legiaofrontal.png
            "default_${cameraType}.png",           // default_frontal.png
            "default${cameraType}.png"             // defaultfrontal.png
        )

        for (fileName in possibleNames) {
            try {
                Log.d("LocalProcessing", "🔍 Procurando overlay: $fileName")
                val inputStream = context.assets.open("overlays/$fileName")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                Log.d("LocalProcessing", "✅ Overlay carregado: $fileName (${bitmap.width}x${bitmap.height})")
                return bitmap
            } catch (e: Exception) {
                // Continua para próximo nome
            }
        }

        Log.e("LocalProcessing", "❌ Nenhum overlay encontrado para: $entityId + $cameraType")
        return null
    }

    private suspend fun overlayEntityImage(
        context: Context,
        imageUri: String,
        entityId: String,
        cameraType: String
    ): String {
        Log.d("LocalProcessing", "🎨 Sobrepondo imagem da entidade...")

        val userPhoto = loadBitmap(context, imageUri) ?: return imageUri
        val overlay = loadEntityOverlay(context, entityId, cameraType)

        val result = if (overlay != null) {
            // Sobrepor overlay na foto do usuário
            combineImages(context, userPhoto, overlay, cameraType)
        } else {
            // Se não há overlay, aplicar efeitos de terror básicos
            Log.d("LocalProcessing", "⚠️ Aplicando efeitos básicos (sem overlay)")
            applyHorrorEffects(userPhoto)
        }

        val outputFile = File(context.cacheDir, "possessed_${System.currentTimeMillis()}.jpg")
        outputFile.outputStream().use { out ->
            result.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        val resultUri = "file://${outputFile.absolutePath}"
        val sizeKB = outputFile.length() / 1024

        Log.d("LocalProcessing", "✅ Imagem salva: ${outputFile.name}")
        Log.d("LocalProcessing", "   Tamanho: ${sizeKB}KB")
        Log.d("LocalProcessing", "   Dimensões: ${result.width}x${result.height}")

        return resultUri
    }

    /**
     * Combina foto do usuário com overlay da entidade
     */
    private suspend fun combineImages(context: Context, base: Bitmap, overlay: Bitmap, cameraType: String): Bitmap {
        val width = base.width
        val height = base.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)

        // CAMADA 1: Desenhar foto original como fundo (levemente escurecida para contraste)
        val baseScaled = if (base.width != width || base.height != height) Bitmap.createScaledBitmap(base, width, height, true) else base
        val bgPaint = android.graphics.Paint().apply {
            colorFilter = android.graphics.ColorMatrixColorFilter(
                android.graphics.ColorMatrix().apply {
                    setScale(0.7f, 0.7f, 0.7f, 1f) // escurecer 30%
                }
            )
        }
        canvas.drawBitmap(baseScaled, 0f, 0f, bgPaint)

        // CAMADA 2: Preparar e desenhar entidade com blur nas bordas
        val overlayScaleFactor = 1.0f
        val scaledOverlayW = (width * overlayScaleFactor).toInt()
        val scaledOverlayH = (height * overlayScaleFactor).toInt()
        val scaledOverlay = Bitmap.createScaledBitmap(overlay, scaledOverlayW, scaledOverlayH, true)
        val overlayOffsetX = -(scaledOverlayW - width) / 2f
        val overlayOffsetY = -(scaledOverlayH - height) / 2f

        val blurredEdges = createEdgeBlurredBitmap(scaledOverlay)

        val overlayAlpha = when {
            cameraType.contains("traseira", ignoreCase = true) -> 150
            else -> 130
        }

        val overlayPaint = android.graphics.Paint().apply {
            isFilterBitmap = true
            alpha = overlayAlpha
            isAntiAlias = true
        }

        canvas.drawBitmap(blurredEdges, overlayOffsetX, overlayOffsetY, overlayPaint)

        // CAMADA 3: Remover fundo da pessoa e desenhar por cima
        Log.d("LocalProcessing", "🎭 Removendo fundo da pessoa...")

        // Tentar usar API Remove.bg primeiro, com fallback para método local
        val personNoBackground = removeBackgroundWithApi(context, baseScaled)
            ?: removeBackgroundLocal(baseScaled)

        val personPaint = android.graphics.Paint().apply {
            isFilterBitmap = true
            isAntiAlias = true
        }
        canvas.drawBitmap(personNoBackground, 0f, 0f, personPaint)

        // Liberar bitmaps temporários
        personNoBackground.recycle()
        blurredEdges.recycle()
        if (scaledOverlay != overlay) scaledOverlay.recycle()
        if (baseScaled != base) baseScaled.recycle()

        return result
    }

    /**
     * Tenta remover fundo usando API Remove.bg
     * Retorna null se API não disponível ou falhar
     */
    private suspend fun removeBackgroundWithApi(context: Context, source: Bitmap): Bitmap? {
        return try {
            // Salvar bitmap temporariamente para enviar à API
            val tempFile = File(context.cacheDir, "temp_for_api_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out ->
                source.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            Log.d("LocalProcessing", "🌐 Tentando API Remove.bg...")
            val result = RemoveBgApi.removeBackground(context, tempFile)

            // Limpar arquivo temporário
            tempFile.delete()

            if (result != null) {
                Log.d("LocalProcessing", "✅ API Remove.bg: sucesso!")

                // Redimensionar para o tamanho original se necessário
                if (result.width != source.width || result.height != source.height) {
                    val resized = Bitmap.createScaledBitmap(result, source.width, source.height, true)
                    result.recycle()
                    return resized
                }
                return result
            } else {
                Log.d("LocalProcessing", "⚠️ API Remove.bg falhou, usando método local")
                return null
            }
        } catch (e: Exception) {
            Log.e("LocalProcessing", "Erro ao usar API: ${e.message}")
            return null
        }
    }

    /**
     * Remove o fundo da imagem de forma mais precisa, mantendo apenas a pessoa
     * Usa análise de contraste e detecção de bordas para identificar a silhueta
     * MÉTODO LOCAL (fallback quando API não disponível)
     */
    private fun removeBackgroundLocal(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height

        // Criar bitmap para análise de luminosidade
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Mapa de luminosidade para detecção de bordas
        val luminosity = FloatArray(width * height)
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            // Fórmula de luminosidade percebida
            luminosity[i] = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
        }

        // Criar máscara de primeiro plano baseada em análise de bordas
        val mask = BooleanArray(width * height) { false }
        val centerX = width / 2
        val centerY = height / 2

        // Região central onde assumimos que está a pessoa (área de interesse)
        val roiLeft = (width * 0.2f).toInt()
        val roiRight = (width * 0.8f).toInt()
        val roiTop = (height * 0.1f).toInt()
        val roiBottom = (height * 0.9f).toInt()

        // Detectar bordas usando diferença de luminosidade
        val edgeThreshold = 0.15f
        val edgeMap = BooleanArray(width * height) { false }

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val current = luminosity[idx]

                // Gradientes horizontais e verticais
                val gx = Math.abs(luminosity[idx + 1] - luminosity[idx - 1])
                val gy = Math.abs(luminosity[idx + width] - luminosity[idx - width])
                val gradient = Math.sqrt((gx * gx + gy * gy).toDouble()).toFloat()

                edgeMap[idx] = gradient > edgeThreshold
            }
        }

        // Região seed (centro da imagem) - assumimos que a pessoa está aqui
        val seedRadius = Math.min(width, height) / 6
        for (y in centerY - seedRadius until centerY + seedRadius) {
            for (x in centerX - seedRadius until centerX + seedRadius) {
                if (x in 0 until width && y in 0 until height) {
                    val dx = x - centerX
                    val dy = y - centerY
                    if (dx * dx + dy * dy < seedRadius * seedRadius) {
                        mask[y * width + x] = true
                    }
                }
            }
        }

        // Expansão da máscara (flood fill simplificado) até encontrar bordas fortes
        val maxIterations = Math.min(width, height) / 3
        for (iteration in 0 until maxIterations) {
            val toExpand = mutableListOf<Int>()

            for (y in 1 until height - 1) {
                for (x in 1 until width - 1) {
                    val idx = y * width + x
                    if (!mask[idx]) {
                        // Verificar se tem vizinho marcado
                        val hasMarkedNeighbor = mask[idx - 1] || mask[idx + 1] ||
                                                mask[idx - width] || mask[idx + width]

                        if (hasMarkedNeighbor) {
                            // Expandir se não for uma borda forte ou se estiver dentro da ROI
                            val isInROI = x in roiLeft..roiRight && y in roiTop..roiBottom
                            val isStrongEdge = edgeMap[idx]

                            if (isInROI && !isStrongEdge) {
                                toExpand.add(idx)
                            }
                        }
                    }
                }
            }

            if (toExpand.isEmpty()) break
            toExpand.forEach { mask[it] = true }
        }

        // Criar bitmap de resultado com máscara suavizada
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val resultPixels = IntArray(width * height)

        // Aplicar suavização (blur) na máscara para bordas mais naturais
        val featherRadius = 8 // pixels de suavização nas bordas

        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x

                if (mask[idx]) {
                    // Calcular alpha baseado na distância até a borda da máscara
                    var minDistToEdge = featherRadius + 1

                    for (dy in -featherRadius..featherRadius) {
                        for (dx in -featherRadius..featherRadius) {
                            val nx = x + dx
                            val ny = y + dy

                            if (nx in 0 until width && ny in 0 until height) {
                                val nidx = ny * width + nx
                                if (!mask[nidx]) {
                                    val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toInt()
                                    minDistToEdge = Math.min(minDistToEdge, dist)
                                }
                            }
                        }
                    }

                    // Alpha baseado na distância até borda (feathering)
                    val alpha = Math.min(255, (minDistToEdge * 255 / featherRadius)).toInt()
                    val originalColor = pixels[idx]
                    val r = (originalColor shr 16) and 0xFF
                    val g = (originalColor shr 8) and 0xFF
                    val b = originalColor and 0xFF

                    resultPixels[idx] = (alpha shl 24) or (r shl 16) or (g shl 8) or b
                } else {
                    resultPixels[idx] = 0 // Transparente
                }
            }
        }

        result.setPixels(resultPixels, 0, width, 0, 0, width, height)

        Log.d("LocalProcessing", "✂️ Fundo removido com detecção de bordas")

        return result
    }

    /**
     * Cria versão da imagem com blur gradual nas bordas
     * Centro permanece nítido, bordas ficam progressivamente borradas
     */
    private fun createEdgeBlurredBitmap(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height

        // Criar versão borrada da imagem inteira
        val blurFactor = 25
        val tinyW = (width / blurFactor).coerceAtLeast(2)
        val tinyH = (height / blurFactor).coerceAtLeast(2)
        val small = Bitmap.createScaledBitmap(source, tinyW, tinyH, true)
        val blurred = Bitmap.createScaledBitmap(small, width, height, true)
        small.recycle()

        // Criar bitmap resultado
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)

        // Desenhar imagem original (nítida) como base
        canvas.drawBitmap(source, 0f, 0f, null)

        // Criar máscara radial: transparente no centro, opaco nas bordas
        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = Math.sqrt((centerX * centerX + centerY * centerY).toDouble()).toFloat()

        // Gradiente radial para a máscara (centro transparente -> bordas opacas)
        val maskPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            shader = android.graphics.RadialGradient(
                centerX, centerY,
                maxRadius * 0.65f, // raio onde começa o blur
                intArrayOf(
                    android.graphics.Color.TRANSPARENT, // centro: sem blur
                    android.graphics.Color.argb(255, 255, 255, 255) // bordas: blur completo
                ),
                floatArrayOf(0.5f, 1f), // transição gradual
                android.graphics.Shader.TileMode.CLAMP
            )
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN)
        }

        // Criar bitmap da versão borrada com máscara aplicada
        val maskedBlur = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempCanvas = android.graphics.Canvas(maskedBlur)
        tempCanvas.drawBitmap(blurred, 0f, 0f, null)
        tempCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), maskPaint)

        // Sobrepor a versão borrada mascarada sobre a imagem nítida
        canvas.drawBitmap(maskedBlur, 0f, 0f, null)

        // Liberar temporários
        blurred.recycle()
        maskedBlur.recycle()

        return result
    }

    private fun applyHorrorEffects(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)

        Log.d("LocalProcessing", "   Aplicando escurecimento...")
        canvas.drawBitmap(original, 0f, 0f, null)

        // 1. Escurecimento geral
        val darkPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(85, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), darkPaint)

        Log.d("LocalProcessing", "   Aplicando vinheta...")
        // 2. Vinheta (escurecimento progressivo nas bordas)
        val vignettePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            shader = android.graphics.RadialGradient(
                width / 2f, height / 2f,
                width.coerceAtLeast(height) * 0.75f,
                intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.argb(95, 0, 0, 0),
                    android.graphics.Color.argb(160, 0, 0, 0)
                ),
                floatArrayOf(0f, 0.6f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)

        Log.d("LocalProcessing", "   Aplicando tons de terror...")
        // 3. Tom avermelhado para atmosfera sinistra
        val redPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(35, 200, 0, 0)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), redPaint)

        // 4. Tom esverdeado nas bordas (efeito sobrenatural)
        val greenPaint = android.graphics.Paint().apply {
            shader = android.graphics.RadialGradient(
                width / 2f, height / 2f,
                width.coerceAtLeast(height) * 0.9f,
                intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.argb(25, 0, 150, 50)
                ),
                floatArrayOf(0.7f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), greenPaint)

        return result
    }
}
