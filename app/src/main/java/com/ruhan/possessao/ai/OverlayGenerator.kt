package com.ruhan.possessao.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.Log
import java.io.File

/**
 * Utilitário para criar overlays placeholder de entidades
 * Use este script para gerar imagens de teste se não tiver imagens prontas
 */
object OverlayGenerator {

    /**
     * Cria overlays placeholder para todas as entidades conhecidas
     * ATENÇÃO: Este é apenas um helper para desenvolvimento
     * Substitua por imagens reais de terror posteriormente
     */
    fun generatePlaceholderOverlays(context: Context) {
        val entities = listOf("legiao", "legiao")
        val cameraTypes = listOf("frontal", "traseira")

        val overlaysDir = File(context.filesDir, "overlays_generated")
        if (!overlaysDir.exists()) {
            overlaysDir.mkdirs()
        }

        entities.forEach { entity ->
            cameraTypes.forEach { camera ->
                val bitmap = createPlaceholderOverlay(entity, camera)
                val file = File(overlaysDir, "${entity}_${camera}.png")
                file.outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                Log.d("OverlayGenerator", "✅ Criado: ${file.name}")
            }
        }

        // Criar defaults também
        cameraTypes.forEach { camera ->
            val bitmap = createDefaultOverlay(camera)
            val file = File(overlaysDir, "default_${camera}.png")
            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            Log.d("OverlayGenerator", "✅ Criado: ${file.name}")
        }

        Log.d("OverlayGenerator", "📁 Overlays salvos em: ${overlaysDir.absolutePath}")
        Log.d("OverlayGenerator", "⚠️  Copie manualmente para app/src/main/assets/overlays/")
    }

    private fun createPlaceholderOverlay(entityId: String, cameraType: String): Bitmap {
        val width = 800
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fundo semi-transparente escuro
        val bgPaint = Paint().apply {
            color = Color.argb(120, 20, 0, 20)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Vinheta de terror
        val vignettePaint = Paint().apply {
            isAntiAlias = true
            shader = android.graphics.RadialGradient(
                width / 2f, height / 2f,
                width * 0.6f,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb(80, 100, 0, 0),
                    Color.argb(160, 50, 0, 0)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)

        // Desenhar símbolo da entidade no centro
        val symbolPaint = Paint().apply {
            color = Color.argb(180, 200, 0, 0)
            style = Paint.Style.STROKE
            strokeWidth = 8f
            isAntiAlias = true
        }

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = 150f

        when (entityId) {
            "legiao" -> {
                // Círculo com X
                canvas.drawCircle(centerX, centerY, radius, symbolPaint)
                canvas.drawLine(centerX - radius, centerY - radius, centerX + radius, centerY + radius, symbolPaint)
                canvas.drawLine(centerX + radius, centerY - radius, centerX - radius, centerY + radius, symbolPaint)
            }
            "legiao" -> {
                // Lua crescente
                val moonPaint = Paint().apply {
                    color = Color.argb(180, 150, 150, 200)
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(centerX, centerY, radius, moonPaint)
                val cutoutPaint = Paint().apply {
                    color = Color.TRANSPARENT
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    isAntiAlias = true
                }
                canvas.drawCircle(centerX + 50f, centerY, radius, cutoutPaint)
            }
            else -> {
                // Símbolo genérico (pentagrama)
                canvas.drawCircle(centerX, centerY, radius, symbolPaint)
                val points = 5
                val angle = Math.PI * 2 / points
                for (i in 0 until points) {
                    val x1 = centerX + (radius * Math.cos(angle * i - Math.PI / 2)).toFloat()
                    val y1 = centerY + (radius * Math.sin(angle * i - Math.PI / 2)).toFloat()
                    val x2 = centerX + (radius * Math.cos(angle * (i + 2) - Math.PI / 2)).toFloat()
                    val y2 = centerY + (radius * Math.sin(angle * (i + 2) - Math.PI / 2)).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, symbolPaint)
                }
            }
        }

        // Texto identificador (para debug)
        val textPaint = Paint().apply {
            color = Color.argb(120, 255, 255, 255)
            textSize = 30f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$entityId ($cameraType)", centerX, height - 50f, textPaint)

        return bitmap
    }

    private fun createDefaultOverlay(cameraType: String): Bitmap {
        val width = 800
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Overlay genérico escuro
        val paint = Paint().apply {
            color = Color.argb(100, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Vinheta
        val vignettePaint = Paint().apply {
            shader = android.graphics.RadialGradient(
                width / 2f, height / 2f,
                width * 0.7f,
                intArrayOf(Color.TRANSPARENT, Color.argb(120, 0, 0, 0)),
                floatArrayOf(0f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)

        return bitmap
    }
}

