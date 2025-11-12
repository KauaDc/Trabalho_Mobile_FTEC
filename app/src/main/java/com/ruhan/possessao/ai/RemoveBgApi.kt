package com.ruhan.possessao.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Integração com a API Remove.bg para remoção profissional de fundo
 * https://www.remove.bg/api
 */
object RemoveBgApi {

    // Coloque sua chave de API aqui (obtenha gratuitamente em https://www.remove.bg/api)
    // Plano gratuito: 50 imagens/mês
    private const val API_KEY = ""
    //5ycXL8eTBYpzNWTLYCenTm4y
    private const val API_URL = "https://api.remove.bg/v1.0/removebg"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Remove o fundo de uma imagem usando a API Remove.bg
     * @param context Contexto Android
     * @param imageFile Arquivo da imagem original
     * @return Bitmap sem fundo ou null se falhar
     */
    suspend fun removeBackground(context: Context, imageFile: File): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("RemoveBgApi", "🌐 Iniciando remoção de fundo via API...")


                // Preparar requisição multipart
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image_file",
                        imageFile.name,
                        imageFile.asRequestBody("image/jpeg".toMediaType())
                    )
                    .addFormDataPart("size", "auto") // ou "preview", "full", "medium", "hd", "4k"
                    .build()

                val request = Request.Builder()
                    .url(API_URL)
                    .addHeader("X-Api-Key", API_KEY)
                    .post(requestBody)
                    .build()

                // Executar requisição
                Log.d("RemoveBgApi", "📤 Enviando imagem para API...")
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e("RemoveBgApi", "❌ Erro na API: ${response.code} - $errorBody")
                    return@withContext null
                }

                // Ler resposta (imagem PNG sem fundo)
                val imageBytes = response.body?.bytes()
                if (imageBytes == null) {
                    Log.e("RemoveBgApi", "❌ Resposta vazia da API")
                    return@withContext null
                }

                // Decodificar bitmap
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                if (bitmap == null) {
                    Log.e("RemoveBgApi", "❌ Falha ao decodificar imagem da API")
                    return@withContext null
                }

                Log.d("RemoveBgApi", "✅ Fundo removido com sucesso! (${bitmap.width}x${bitmap.height})")

                // Opcional: salvar para debug
                saveDebugImage(context, bitmap, "removebg_result")

                return@withContext bitmap

            } catch (e: Exception) {
                Log.e("RemoveBgApi", "❌ Exceção ao remover fundo: ${e.message}", e)
                return@withContext null
            }
        }
    }

    /**
     * Salva imagem para debug
     */
    private fun saveDebugImage(context: Context, bitmap: Bitmap, prefix: String) {
        try {
            val debugFile = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.png")
            FileOutputStream(debugFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.d("RemoveBgApi", "💾 Debug salvo: ${debugFile.name}")
        } catch (e: Exception) {
            Log.e("RemoveBgApi", "Erro ao salvar debug: ${e.message}")
        }
    }
}

