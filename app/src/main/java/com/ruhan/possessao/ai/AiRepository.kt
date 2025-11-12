package com.ruhan.possessao.ai

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Repositório otimizado para processar imagens usando API Gemini
 * Baseado no exemplo oficial Go do Google: https://ai.google.dev/
 *
 * MODELO: gemini-2.5-flash-image
 *
 * ⚠️ IMPORTANTE SOBRE GERAÇÃO DE IMAGENS:
 * ─────────────────────────────────────────────────────────────────
 * O modelo "gemini-2.5-flash-image" pode ser usado para:
 *   ✅ ANÁLISE de imagens (image understanding)
 *   ❓ GERAÇÃO de imagens (pode não suportar ainda)
 *
 * Conforme exemplo Go oficial, a resposta esperada é:
 *   result.Candidates[0].Content.Parts → InlineData.Data (imagem gerada)
 *
 * Se a API não retornar imagem gerada, o app usa FALLBACK LOCAL
 * com efeitos de terror aplicados offline (sempre funciona).
 * ─────────────────────────────────────────────────────────────────
 *
 * LIMITES OFICIAIS DA API GEMINI:
 * ─────────────────────────────────────────────────────────────────
 * INLINE (Base64):
 *   • Limite: 20 MB total da requisição (prompt + imagem + JSON)
 *   • Recomendado: < 5 MB para margem de segurança
 *   • Ideal para: Imagens pequenas, uso único
 *
 * FILE API (Upload separado):
 *   • Limite: Arquivos maiores (até centenas de MB)
 *   • Recomendado: > 1 MB ou reutilização
 *   • Ideal para: Arquivos grandes, múltiplas requisições
 *
 * QUOTA OBSERVADA:
 *   • RPM: 10 requests/minuto
 *   • TPM: 200.000 tokens/minuto
 *   • RPD: 100 requests/dia
 * ─────────────────────────────────────────────────────────────────
 *
 * ESTRATÉGIA IMPLEMENTADA:
 *   1. Reduzir imagem para 256px WebP 60% (~8-12KB)
 *   2. Se < 1 MB: usar INLINE (mais rápido, 1 request)
 *   3. Se > 1 MB: usar FILE API (upload + generate, 2 requests)
 *   4. Se API não retornar imagem: FALLBACK LOCAL (sempre funciona)
 */
object AiRepository {
    // Sua chave de API do Google AI Studio
    private const val API_KEY = "AIzaSyBnjW4aj1b2V3cvD_1VtN1Yqe6cTiqurIk"

    // Modelo funcionando
    private const val MODEL = "gemini-2.5-flash-image"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1alpha"
    private const val ENDPOINT = "$BASE_URL/models/$MODEL:generateContent"

    // File API - Upload separado (para arquivos > 1 MB)
    private const val FILE_API_URL = "https://generativelanguage.googleapis.com/upload/v1beta/files"

    // Configurações otimizadas para ficar MUITO abaixo do limite de 20 MB
    private const val MAX_IMAGE_DIMENSION = 256  // 256px = ~8-12KB WebP
    private const val WEBP_QUALITY = 60          // WebP 60% = ótima compressão
    private const val USE_WEBP = true            // WebP economiza 25-35% vs JPEG
    private const val MAX_RETRIES = 2            // 2 tentativas
    private const val INITIAL_BACKOFF_MS = 4000L // Backoff 4s

    // LIMITES PARA ESCOLHA DE MÉTODO
    private const val INLINE_MAX_SIZE_MB = 5.0   // Máximo 5 MB para inline (margem de segurança)
    private const val FILE_API_MIN_SIZE_MB = 1.0 // Usar File API se > 1 MB

    // Estimativa: 256px WebP 60% ≈ 8-12KB (~200 tokens)
    // Muito abaixo dos 20 MB inline e ideal para performance

    // Método de envio de imagem
    enum class ImageUploadMethod {
        INLINE_BASE64,      // Base64 direto no JSON (simples, mas maior)
        FILE_API,           // Upload via File API (mais eficiente)
        MULTIPART_FORM      // Multipart form data (alternativa)
    }
    private var uploadMethod = ImageUploadMethod.INLINE_BASE64

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Faz upload da imagem usando File API do Gemini
     * Retorna o URI do arquivo para usar na requisição
     *
     * VANTAGEM: Upload separado, depois só envia URI (muito menor que base64)
     */
    private suspend fun uploadImageViaFileApi(context: Context, imageFile: File): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AiRepository", "📤 Fazendo upload via File API...")

                val optimizedBytes = optimizeImageForApi(imageFile)
                val sizeKB = optimizedBytes.size / 1024
                val format = if (USE_WEBP) "WebP" else "JPEG"
                Log.d("AiRepository", "📊 Tamanho: ${sizeKB}KB ($format)")

                // Estimar tokens (aproximado: 1KB ≈ 15-20 tokens para imagem)
                val estimatedTokens = sizeKB * 17 // média
                Log.d("AiRepository", "📊 Tokens estimados: ~$estimatedTokens (limite: 32.768)")

                val mimeType = if (USE_WEBP) "image/webp" else "image/jpeg"
                val fileName = if (USE_WEBP) "image.webp" else "image.jpg"

                // Construir metadata JSON (parte 1 do multipart)
                val metadata = JSONObject().apply {
                    put("file", JSONObject().apply {
                        put("display_name", fileName)
                    })
                }

                // Construir multipart request com metadata + file (formato correto da API)
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "metadata",
                        null,
                        metadata.toString().toRequestBody("application/json".toMediaType())
                    )
                    .addFormDataPart(
                        "file",
                        fileName,
                        optimizedBytes.toRequestBody(mimeType.toMediaType())
                    )
                    .build()

                val url = "$FILE_API_URL?key=$API_KEY"
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("X-Goog-Upload-Protocol", "multipart")
                    .build()

                val startTime = System.currentTimeMillis()
                client.newCall(request).execute().use { response ->
                    val duration = System.currentTimeMillis() - startTime
                    val body = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val json = JSONObject(body)
                        val fileUri = json.getJSONObject("file").getString("uri")

                        Log.d("AiRepository", "✅ Upload concluído! (${duration}ms)")
                        Log.d("AiRepository", "📎 File URI: $fileUri")

                        saveDebugResponse(context, body, "file_upload")
                        return@withContext fileUri
                    } else {
                        Log.e("AiRepository", "❌ Erro ${response.code} no upload")
                        Log.e("AiRepository", "Resposta: $body")
                        saveDebugResponse(context, body, "file_upload_error")
                    }
                }
            } catch (e: Exception) {
                Log.e("AiRepository", "❌ Exceção no upload: ${e.message}", e)
            }
            null
        }
    }

    /**
     * Faz upload da imagem via multipart form data
     * Alternativa ao File API
     */
    private suspend fun uploadImageViaMultipart(imageFile: File): ByteArray {
        return withContext(Dispatchers.IO) {
            Log.d("AiRepository", "📤 Preparando upload multipart...")
            optimizeImageForApi(imageFile)
        }
    }
    suspend fun listAvailableModels(context: Context): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AiRepository", "📋 Listando modelos disponíveis...")

                val url = "$BASE_URL/models?key=$API_KEY"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val json = JSONObject(body)
                        val models = mutableListOf<String>()

                        if (json.has("models")) {
                            val modelsArray = json.getJSONArray("models")
                            for (i in 0 until modelsArray.length()) {
                                val model = modelsArray.getJSONObject(i)
                                val name = model.optString("name", "")
                                val displayName = model.optString("displayName", "")

                                Log.d("AiRepository", "  ✓ $displayName")
                                Log.d("AiRepository", "    ID: $name")

                                models.add(name)
                            }
                        }

                        Log.d("AiRepository", "✓ Total: ${models.size} modelos")
                        saveDebugResponse(context, body, "list_models")
                        return@withContext models
                    } else {
                        Log.e("AiRepository", "❌ Erro ${response.code} ao listar modelos")
                        Log.e("AiRepository", "Resposta: $body")
                    }
                }
            } catch (e: Exception) {
                Log.e("AiRepository", "❌ Exceção: ${e.message}", e)
            }
            emptyList()
        }
    }

    /**
     * Verifica informações e limites de quota do modelo
     */
    suspend fun checkModelQuota(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AiRepository", "📊 Verificando quota de: $MODEL")

                val url = "$BASE_URL/models/$MODEL?key=$API_KEY"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val json = JSONObject(body)
                        Log.d("AiRepository", "✓ Modelo: ${json.optString("displayName")}")

                        // Exibir limites
                        if (json.has("inputTokenLimit")) {
                            val inputLimit = json.getInt("inputTokenLimit")
                            Log.d("AiRepository", "  📥 Input limit: $inputLimit tokens")
                        }
                        if (json.has("outputTokenLimit")) {
                            val outputLimit = json.getInt("outputTokenLimit")
                            Log.d("AiRepository", "  📤 Output limit: $outputLimit tokens")
                        }

                        saveDebugResponse(context, body, "model_quota")
                        return@withContext body
                    } else {
                        Log.e("AiRepository", "❌ Erro ${response.code}")
                        Log.e("AiRepository", "Resposta: $body")
                    }
                }
            } catch (e: Exception) {
                Log.e("AiRepository", "❌ Exceção: ${e.message}", e)
            }
            null
        }
    }

    /**
     * Processa a imagem aplicando efeitos de terror via API Gemini.
     * Com fallback para processamento local se API falhar.
     */
    suspend fun processImage(context: Context, imageUri: String, prompt: String, checkQuota: Boolean = false): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AiRepository", "🎬 Processando imagem")
                Log.d("AiRepository", "📝 Prompt: $prompt")

                // Verificar quota se solicitado (debug mode)
                if (checkQuota) {
                    Log.d("AiRepository", "🔍 Verificando quota disponível...")
                    checkModelQuota(context)
                }

                // Tentar usar API com retry inteligente
                val apiResult = tryApiWithRetry(context, imageUri, prompt)
                if (apiResult != null) {
                    Log.d("AiRepository", "✅ Sucesso via API!")
                    return@withContext apiResult
                }

                // Se chegou aqui, API falhou (provavelmente erro 429)
                Log.w("AiRepository", "")
                Log.w("AiRepository", "╔══════════════════════════════════════════════════════╗")
                Log.w("AiRepository", "║  ⚠️  QUOTA DA API GEMINI ESGOTADA                   ║")
                Log.w("AiRepository", "╠══════════════════════════════════════════════════════╣")
                Log.w("AiRepository", "║                                                      ║")
                Log.w("AiRepository", "║  Possíveis causas:                                   ║")
                Log.w("AiRepository", "║  • Limite de requisições por minuto (RPM)           ║")
                Log.w("AiRepository", "║  • Limite de tokens por minuto (TPM)                ║")
                Log.w("AiRepository", "║  • Limite diário de requisições (RPD)               ║")
                Log.w("AiRepository", "║                                                      ║")
                Log.w("AiRepository", "║  Soluções:                                           ║")
                Log.w("AiRepository", "║  1. Aguarde 1-2 minutos e tente novamente          ║")
                Log.w("AiRepository", "║  2. Se persistir, aguarde até amanhã (reset diário) ║")
                Log.w("AiRepository", "║  3. Verifique quota em: ai.google.dev              ║")
                Log.w("AiRepository", "║  4. Considere upgrade da conta (mais quota)         ║")
                Log.w("AiRepository", "║                                                      ║")
                Log.w("AiRepository", "║  Enquanto isso, usando processamento local...       ║")
                Log.w("AiRepository", "║                                                      ║")
                Log.w("AiRepository", "╚══════════════════════════════════════════════════════╝")
                Log.w("AiRepository", "")

                // Fallback: processamento local
                Log.i("AiRepository", "🎨 Usando processamento local (efeitos de terror)")
                return@withContext processImageLocally(context, imageUri, prompt)

            } catch (e: Exception) {
                Log.e("AiRepository", "❌ Erro: ${e.message}", e)
                return@withContext processImageLocally(context, imageUri, prompt)
            }
        }
    }

    /**
     * Tenta usar a API com retry inteligente e backoff exponencial
     * MODO TESTE: Enviando apenas texto (sem imagem)
     */
    private suspend fun tryApiWithRetry(
        context: Context,
        imageUri: String,
        prompt: String
    ): String? {
        return withContext(Dispatchers.IO) {
            for (attempt in 1..MAX_RETRIES) {
                try {
                    Log.d("AiRepository", "🔄 Tentativa $attempt/$MAX_RETRIES")

                    // ═══════════════════════════════════════════════════════
                    // MODO TESTE: APENAS TEXTO (SEM IMAGEM)
                    // ═══════════════════════════════════════════════════════
                    Log.w("AiRepository", "")
                    Log.w("AiRepository", "╔════════════════════════════════════════════╗")
                    Log.w("AiRepository", "║  🧪 MODO TESTE ATIVO                      ║")
                    Log.w("AiRepository", "║  Enviando APENAS TEXTO (sem imagem)       ║")
                    Log.w("AiRepository", "╚════════════════════════════════════════════╝")
                    Log.w("AiRepository", "")

                    // Construir JSON com APENAS TEXTO
                    val json = buildRequestJsonTextOnly(prompt)

                    Log.d("AiRepository", "📝 Prompt: $prompt")
                    Log.d("AiRepository", "🔧 Método: TEXT ONLY (teste)")

                    // ═══════════════════════════════════════════════════════
                    // COMENTADO: Código original com imagem
                    // ═══════════════════════════════════════════════════════
                    /*
                    // 1. Carregar e otimizar imagem
                    val file = loadImageFile(context, imageUri) ?: run {
                        Log.e("AiRepository", "❌ Falha ao carregar arquivo")
                        return@withContext null
                    }

                    val optimizedBytes = optimizeImageForApi(file)
                    val sizeMB = optimizedBytes.size / (1024.0 * 1024.0)

                    // 2. ESCOLHER MÉTODO AUTOMATICAMENTE baseado no tamanho
                    val chosenMethod = when {
                        sizeMB > INLINE_MAX_SIZE_MB -> {
                            Log.w("AiRepository", "⚠️ Imagem ${sizeMB.format(2)}MB > ${INLINE_MAX_SIZE_MB}MB")
                            Log.w("AiRepository", "   Muito grande para inline, usando File API")
                            ImageUploadMethod.FILE_API
                        }
                        sizeMB > FILE_API_MIN_SIZE_MB -> {
                            Log.d("AiRepository", "📤 Imagem ${sizeMB.format(2)}MB > ${FILE_API_MIN_SIZE_MB}MB")
                            Log.d("AiRepository", "   Recomendado usar File API")
                            ImageUploadMethod.FILE_API
                        }
                        else -> {
                            Log.d("AiRepository", "📥 Imagem ${sizeMB.format(2)}MB < ${FILE_API_MIN_SIZE_MB}MB")
                            Log.d("AiRepository", "   Ideal para inline (mais rápido)")
                            ImageUploadMethod.INLINE_BASE64
                        }
                    }

                    // 3. Executar método escolhido
                    val json = when (chosenMethod) {
                        ImageUploadMethod.FILE_API -> {
                            Log.d("AiRepository", "🔧 Método: File API (upload separado)")
                            val fileUri = uploadImageViaFileApi(context, file)
                            if (fileUri != null) {
                                buildRequestJsonWithFileUri(prompt, fileUri)
                            } else {
                                Log.w("AiRepository", "⚠️ File API falhou, usando base64")
                                val b64 = Base64.encodeToString(optimizedBytes, Base64.NO_WRAP)
                                logImageSizes(file, optimizedBytes, b64)
                                buildRequestJsonWithBase64(prompt, b64)
                            }
                        }

                        ImageUploadMethod.INLINE_BASE64 -> {
                            Log.d("AiRepository", "🔧 Método: Base64 inline")
                            val b64 = Base64.encodeToString(optimizedBytes, Base64.NO_WRAP)
                            logImageSizes(file, optimizedBytes, b64)
                            buildRequestJsonWithBase64(prompt, b64)
                        }

                        ImageUploadMethod.MULTIPART_FORM -> {
                            // Fallback para base64
                            val b64 = Base64.encodeToString(optimizedBytes, Base64.NO_WRAP)
                            logImageSizes(file, optimizedBytes, b64)
                            buildRequestJsonWithBase64(prompt, b64)
                        }
                    }
                    */
                    // ═══════════════════════════════════════════════════════

                    // 4. Construir request
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = json.toRequestBody(mediaType)

                    val url = "$ENDPOINT?key=$API_KEY"
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .addHeader("Content-Type", "application/json")
                        .build()

                    // 5. Executar chamada
                    Log.d("AiRepository", "🌐 Chamando API Gemini...")
                    val startTime = System.currentTimeMillis()

                    val response = client.newCall(request).execute()
                    val duration = System.currentTimeMillis() - startTime
                    val respBody = response.body?.string() ?: ""

                    // Salvar resposta para debug
                    saveDebugResponse(context, respBody, "response_attempt_$attempt")

                    val shouldRetry = when (response.code) {
                        200 -> {
                            Log.d("AiRepository", "✅ Sucesso! (${duration}ms)")
                            val imageUri = extractImageFromResponse(context, respBody)
                            if (imageUri != null) {
                                response.close()
                                return@withContext imageUri
                            }
                            Log.w("AiRepository", "⚠️ Resposta OK mas sem imagem")
                            false
                        }

                        429 -> {
                            Log.w("AiRepository", "⚠️ Erro 429: Quota/Rate limit excedido")
                            val isQuotaExceeded = respBody.contains("RESOURCE_EXHAUSTED") ||
                                                 respBody.contains("quota", ignoreCase = true)

                            if (isQuotaExceeded) {
                                Log.e("AiRepository", "❌ QUOTA DIÁRIA ESGOTADA - não adianta retry")
                                false
                            } else {
                                Log.w("AiRepository", "⚠️ Rate limit temporário - aguardando...")
                                attempt < MAX_RETRIES
                            }
                        }

                        400 -> {
                            Log.e("AiRepository", "❌ Erro 400: Request inválido")
                            Log.e("AiRepository", "Resposta: $respBody")
                            false
                        }

                        401, 403 -> {
                            Log.e("AiRepository", "❌ Erro ${response.code}: Autenticação falhou")
                            false
                        }

                        404 -> {
                            Log.e("AiRepository", "❌ Erro 404: Modelo $MODEL não encontrado")
                            false
                        }

                        else -> {
                            Log.e("AiRepository", "❌ Erro ${response.code}")
                            Log.e("AiRepository", "Resposta: $respBody")
                            attempt < MAX_RETRIES
                        }
                    }

                    response.close()

                    if (shouldRetry) {
                        val baseDelay = INITIAL_BACKOFF_MS * (1 shl (attempt - 1))
                        val jitter = (Math.random() * 1000).toLong()
                        val delay = baseDelay + jitter
                        Log.d("AiRepository", "⏳ Aguardando ${delay}ms...")
                        kotlinx.coroutines.delay(delay)
                    } else {
                        return@withContext null
                    }

                } catch (e: Exception) {
                    Log.e("AiRepository", "❌ Exceção: ${e.message}", e)
                    if (attempt < MAX_RETRIES) {
                        kotlinx.coroutines.delay(1000L * attempt)
                    } else {
                        return@withContext null
                    }
                }
            }

            Log.w("AiRepository", "❌ Todas as tentativas falharam")
            null
        }
    }

    // Helper para formatar Double
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    /**
     * Helper para logar tamanhos de imagem e alertar se próximo do limite
     */
    private fun logImageSizes(file: File, optimizedBytes: ByteArray, base64: String) {
        val originalKB = file.length() / 1024
        val optimizedKB = optimizedBytes.size / 1024
        val base64KB = base64.length / 1024
        val reduction = 100 - (optimizedBytes.size * 100 / file.length())

        // Estimar tokens (aproximado: 1KB ≈ 15-20 tokens)
        val estimatedTokens = optimizedKB * 17

        Log.d("AiRepository", "📊 Original: ${originalKB}KB")
        Log.d("AiRepository", "📊 Otimizado: ${optimizedKB}KB (${reduction}% redução)")
        Log.d("AiRepository", "📊 Base64: ${base64KB}KB (~${base64.length} chars)")
        Log.d("AiRepository", "📊 Tokens estimados: ~$estimatedTokens")

        // Alertar se próximo de limites perigosos
        when {
            optimizedKB > 20 -> {
                Log.w("AiRepository", "⚠️ AVISO: Imagem grande (${optimizedKB}KB)!")
                Log.w("AiRepository", "   Pode estourar limite de tokens!")
                Log.w("AiRepository", "   Recomendado: < 15KB")
            }
            optimizedKB > 15 -> {
                Log.w("AiRepository", "⚠️ Imagem média-grande (${optimizedKB}KB)")
                Log.w("AiRepository", "   Próximo do limite seguro")
            }
            else -> {
                Log.d("AiRepository", "✅ Tamanho OK! (${optimizedKB}KB < 15KB)")
            }
        }
    }

    /**
     * Processa a imagem localmente adicionando efeitos de terror.
     * Este método SEMPRE funciona, independente de APIs externas.
     */
    private suspend fun processImageLocally(
        context: Context,
        imageUri: String,
        prompt: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AiRepository", "🎨 Processando imagem localmente com efeitos de terror")

                val originalBitmap = loadBitmapFromUri(context, imageUri) ?: run {
                    Log.e("AiRepository", "Falha ao carregar bitmap")
                    return@withContext imageUri
                }

                // Aplicar efeitos de terror
                val processedBitmap = applyHorrorEffects(originalBitmap)

                // Salvar imagem processada
                val outputFile = File(context.cacheDir, "horror_${System.currentTimeMillis()}.jpg")
                outputFile.outputStream().use { out ->
                    processedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
                }

                val resultUri = "file://${outputFile.absolutePath}"
                Log.d("AiRepository", "✓ Imagem processada localmente: $resultUri")

                return@withContext resultUri
            } catch (e: Exception) {
                Log.e("AiRepository", "Erro no processamento local: ${e.message}", e)
                return@withContext imageUri
            }
        }
    }

    private fun loadBitmapFromUri(context: Context, imageUri: String): android.graphics.Bitmap? {
        return try {
            when {
                imageUri.startsWith("file://") -> {
                    android.graphics.BitmapFactory.decodeFile(imageUri.removePrefix("file://"))
                }
                imageUri.startsWith("content://") -> {
                    context.contentResolver.openInputStream(imageUri.toUri())?.use {
                        android.graphics.BitmapFactory.decodeStream(it)
                    }
                }
                else -> {
                    android.graphics.BitmapFactory.decodeFile(imageUri)
                }
            }
        } catch (e: Exception) {
            Log.e("AiRepository", "Erro ao carregar bitmap: ${e.message}", e)
            null
        }
    }

    /**
     * Aplica efeitos de terror na imagem:
     * - Escurecimento geral
     * - Vinheta escura nas bordas
     * - Tom avermelhado/esverdeado
     * - Grão/ruído para atmosfera assustadora
     */
    private fun applyHorrorEffects(original: android.graphics.Bitmap): android.graphics.Bitmap {
        val width = original.width
        val height = original.height
        val result = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)

        // Desenhar imagem original
        canvas.drawBitmap(original, 0f, 0f, null)

        // 1. Overlay escuro semi-transparente
        val darkPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(70, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), darkPaint)

        // 2. Vinheta (escurecimento progressivo nas bordas)
        val vignettePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            shader = android.graphics.RadialGradient(
                width / 2f, height / 2f,
                width.coerceAtLeast(height) * 0.75f,
                intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.argb(80, 0, 0, 0),
                    android.graphics.Color.argb(140, 0, 0, 0)
                ),
                floatArrayOf(0f, 0.6f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)

        // 3. Tom avermelhado para atmosfera sinistra
        val redPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(25, 200, 0, 0)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), redPaint)

        // 4. Leve tom esverdeado nas bordas (efeito sobrenatural)
        val greenPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(15, 0, 100, 0)
            shader = android.graphics.RadialGradient(
                width / 2f, height / 2f,
                width.coerceAtLeast(height) * 0.9f,
                intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.argb(15, 0, 100, 0)
                ),
                floatArrayOf(0f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), greenPaint)

        return result
    }

    /**
     * Otimiza a imagem para envio à API:
     * - Redimensiona para máximo 512px (mantendo proporção)
     * - Comprime para WebP 75% ou JPEG 60% (WebP é 25-35% menor)
     * - Garante que fica muito abaixo dos 32.768 tokens de limite
     */
    private fun optimizeImageForApi(imageFile: File): ByteArray {
        try {
            // Carregar bitmap
            val originalBitmap = android.graphics.BitmapFactory.decodeFile(imageFile.absolutePath)
                ?: return imageFile.readBytes()

            val originalWidth = originalBitmap.width
            val originalHeight = originalBitmap.height

            // Calcular novas dimensões
            val scale = if (originalWidth > originalHeight) {
                if (originalWidth > MAX_IMAGE_DIMENSION) MAX_IMAGE_DIMENSION.toFloat() / originalWidth else 1f
            } else {
                if (originalHeight > MAX_IMAGE_DIMENSION) MAX_IMAGE_DIMENSION.toFloat() / originalHeight else 1f
            }

            val newWidth = (originalWidth * scale).toInt()
            val newHeight = (originalHeight * scale).toInt()

            // Redimensionar se necessário
            val resizedBitmap = if (scale < 1f) {
                Log.d("AiRepository", "🔧 Redimensionando: ${originalWidth}x${originalHeight} → ${newWidth}x${newHeight}")
                android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                Log.d("AiRepository", "✓ Tamanho adequado: ${originalWidth}x${originalHeight}")
                originalBitmap
            }

            // Comprimir usando formato otimizado
            val outputStream = java.io.ByteArrayOutputStream()
            if (USE_WEBP && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // WebP com suporte moderno (Android 11+)
                resizedBitmap.compress(
                    android.graphics.Bitmap.CompressFormat.WEBP_LOSSY,
                    WEBP_QUALITY,
                    outputStream
                )
                Log.d("AiRepository", "🎨 Formato: WebP Lossy $WEBP_QUALITY%")
            } else if (USE_WEBP && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // WebP legacy (Android 4.3+)
                resizedBitmap.compress(
                    android.graphics.Bitmap.CompressFormat.WEBP,
                    WEBP_QUALITY,
                    outputStream
                )
                Log.d("AiRepository", "🎨 Formato: WebP $WEBP_QUALITY%")
            } else {
                // Fallback para JPEG (Android antigo ou se WebP desabilitado)
                resizedBitmap.compress(
                    android.graphics.Bitmap.CompressFormat.JPEG,
                    60,
                    outputStream
                )
                Log.d("AiRepository", "🎨 Formato: JPEG 60% (fallback)")
            }

            val compressedBytes = outputStream.toByteArray()

            // Liberar memória
            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle()
            }
            originalBitmap.recycle()

            return compressedBytes

        } catch (e: Exception) {
            Log.e("AiRepository", "❌ Erro ao otimizar: ${e.message}", e)
            return imageFile.readBytes()
        }
    }

    private fun loadImageFile(context: Context, imageUri: String): File? {
        return try {
            when {
                imageUri.startsWith("file://") -> {
                    File(imageUri.removePrefix("file://"))
                }
                imageUri.startsWith("content://") -> {
                    val input = context.contentResolver.openInputStream(imageUri.toUri())
                        ?: return null
                    val tmp = File.createTempFile("upload", ".jpg", context.cacheDir)
                    input.use { inp ->
                        tmp.outputStream().use { out ->
                            inp.copyTo(out)
                        }
                    }
                    tmp
                }
                imageUri.startsWith("http") -> {
                    val req = Request.Builder().url(imageUri).build()
                    client.newCall(req).execute().use { resp ->
                        if (!resp.isSuccessful) return null
                        val tmp = File.createTempFile("download", ".jpg", context.cacheDir)
                        resp.body?.byteStream()?.use { ins ->
                            tmp.outputStream().use { out ->
                                ins.copyTo(out)
                            }
                        }
                        tmp
                    }
                }
                else -> File(imageUri)
            }
        } catch (e: Exception) {
            Log.e("AiRepository", "Erro ao carregar arquivo: ${e.message}", e)
            null
        }
    }

    /**
     * Constrói JSON de requisição usando base64 inline
     * Detecta formato automaticamente (WebP ou JPEG)
     */
    private fun buildRequestJsonWithBase64(prompt: String, imageBase64: String): String {
        val mimeType = if (USE_WEBP) "image/webp" else "image/jpeg"
        return """
        {
            "contents": [{
                "parts": [
                    {
                        "text": "$prompt"
                    },
                    {
                        "inline_data": {
                            "mime_type": "$mimeType",
                            "data": "$imageBase64"
                        }
                    }
                ]
            }],
            "generationConfig": {
                "temperature": 0.9,
                "topK": 40,
                "topP": 0.95,
                "maxOutputTokens": 8192
            }
        }
        """.trimIndent()
    }

    /**
     * Constrói JSON de requisição com APENAS TEXTO (sem imagem)
     * PARA TESTE: Verificar se o modelo está funcionando
     */
    private fun buildRequestJsonTextOnly(prompt: String): String {
        return """
        {
            "contents": [{
                "parts": [
                    {
                        "text": "$prompt"
                    }
                ]
            }],
            "generationConfig": {
                "temperature": 0.9,
                "topK": 40,
                "topP": 0.95,
                "maxOutputTokens": 8192
            }
        }
        """.trimIndent()
    }

    /**
     * Constrói JSON de requisição usando File URI
     * O URI vem do upload prévio via File API
     */
    private fun buildRequestJsonWithFileUri(prompt: String, fileUri: String): String {
        val mimeType = if (USE_WEBP) "image/webp" else "image/jpeg"
        return """
        {
            "contents": [{
                "parts": [
                    {
                        "text": "$prompt"
                    },
                    {
                        "file_data": {
                            "mime_type": "$mimeType",
                            "file_uri": "$fileUri"
                        }
                    }
                ]
            }],
            "generationConfig": {
                "temperature": 0.9,
                "topK": 40,
                "topP": 0.95,
                "maxOutputTokens": 8192
            }
        }
        """.trimIndent()
    }

    private fun saveDebugResponse(context: Context, response: String, prefix: String = "response") {
        try {
            val timestamp = System.currentTimeMillis()
            val debugFile = File(context.cacheDir, "ai_${prefix}_${timestamp}.json")
            debugFile.writeText(response)
            Log.d("AiRepository", "💾 Debug salvo: ${debugFile.name}")
        } catch (e: Exception) {
            Log.w("AiRepository", "⚠️ Erro ao salvar debug: ${e.message}")
        }
    }

    /**
     * Extrai imagem gerada da resposta da API
     * Baseado no exemplo oficial Go do Google Gemini
     * Estrutura: result.Candidates[0].Content.Parts → InlineData.Data
     */
    private fun extractImageFromResponse(context: Context, jsonResponse: String): String? {
        return try {
            val json = JSONObject(jsonResponse)

            Log.d("AiRepository", "🔍 Analisando resposta da API...")

            // 1. Verificar candidates
            if (!json.has("candidates")) {
                Log.w("AiRepository", "⚠️ Resposta não contém 'candidates'")
                Log.d("AiRepository", "Estrutura: ${json.keys().asSequence().toList()}")
                return null
            }

            val candidates = json.getJSONArray("candidates")
            if (candidates.length() == 0) {
                Log.w("AiRepository", "⚠️ Array 'candidates' está vazio")
                return null
            }

            Log.d("AiRepository", "✓ Encontrado ${candidates.length()} candidate(s)")

            // 2. Verificar content no primeiro candidate
            val firstCandidate = candidates.getJSONObject(0)
            if (!firstCandidate.has("content")) {
                Log.w("AiRepository", "⚠️ Candidate não contém 'content'")
                Log.d("AiRepository", "Keys: ${firstCandidate.keys().asSequence().toList()}")

                // Verificar se há finishReason ou outros campos informativos
                if (firstCandidate.has("finishReason")) {
                    Log.w("AiRepository", "   finishReason: ${firstCandidate.getString("finishReason")}")
                }
                return null
            }

            val content = firstCandidate.getJSONObject("content")
            if (!content.has("parts")) {
                Log.w("AiRepository", "⚠️ Content não contém 'parts'")
                return null
            }

            val parts = content.getJSONArray("parts")
            Log.d("AiRepository", "✓ Encontrado ${parts.length()} part(s)")

            // 3. Procurar por inline_data com imagem (conforme exemplo Go)
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)

                Log.d("AiRepository", "   Part $i: ${part.keys().asSequence().toList()}")

                // Verificar inline_data (onde vem a imagem gerada)
                if (part.has("inline_data") || part.has("inlineData")) {
                    val inlineData = if (part.has("inline_data")) {
                        part.getJSONObject("inline_data")
                    } else {
                        part.getJSONObject("inlineData")
                    }

                    if (inlineData.has("data") && inlineData.has("mimeType")) {
                        val base64Data = inlineData.getString("data")
                        val mimeType = inlineData.getString("mimeType")

                        if (mimeType.startsWith("image/")) {
                            Log.d("AiRepository", "✅ Encontrada imagem: $mimeType")
                            Log.d("AiRepository", "   Tamanho base64: ${base64Data.length} chars")
                            return saveBase64Image(context, base64Data, mimeType)
                        }
                    } else if (inlineData.has("data") && inlineData.has("mime_type")) {
                        // Alternativa: mime_type com underscore
                        val base64Data = inlineData.getString("data")
                        val mimeType = inlineData.getString("mime_type")

                        if (mimeType.startsWith("image/")) {
                            Log.d("AiRepository", "✅ Encontrada imagem: $mimeType")
                            Log.d("AiRepository", "   Tamanho base64: ${base64Data.length} chars")
                            return saveBase64Image(context, base64Data, mimeType)
                        }
                    }
                }

                // Verificar se há texto (para debug)
                if (part.has("text")) {
                    val text = part.getString("text")
                    Log.d("AiRepository", "   Part $i contém texto: ${text.take(100)}...")
                }
            }

            Log.w("AiRepository", "⚠️ Nenhuma imagem encontrada nos parts")
            Log.w("AiRepository", "   Modelo: $MODEL")
            Log.w("AiRepository", "   NOTA: O modelo gemini-2.5-flash-image pode não gerar imagens,")
            Log.w("AiRepository", "   apenas analisar. Verifique se o modelo suporta image generation.")
            null
        } catch (e: Exception) {
            Log.e("AiRepository", "❌ Erro ao extrair imagem: ${e.message}", e)
            null
        }
    }

    private fun saveBase64Image(context: Context, base64Data: String, mimeType: String): String? {
        return try {
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val extension = when {
                mimeType.contains("png") -> ".png"
                mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
                mimeType.contains("webp") -> ".webp"
                else -> ".jpg"
            }

            val outputFile = File(context.cacheDir, "ai_generated_${System.currentTimeMillis()}$extension")
            outputFile.outputStream().use { it.write(bytes) }

            val uri = "file://${outputFile.absolutePath}"
            Log.d("AiRepository", "Imagem salva em: $uri")
            uri
        } catch (e: Exception) {
            Log.e("AiRepository", "Erro ao salvar imagem base64: ${e.message}", e)
            null
        }
    }
}
