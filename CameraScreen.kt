package com.ruhan.possessao.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

@Composable
fun CameraScreen(onPhotoCaptured: (String, String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasCameraPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission.value = granted }
    )

    val executor = remember { Executors.newSingleThreadExecutor() }
    // Referências lembradas para PreviewView e ImageCapture para permitir rebind
    val previewViewRef = remember { mutableStateOf<androidx.camera.view.PreviewView?>(null) }
    val imageCaptureRef = remember { mutableStateOf<ImageCapture?>(null) }

    // Estado para alternar câmera frontal/traseira
    var useFrontCamera by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Registro imagético solene",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Toggle para alternar câmera
                TextButton(onClick = { useFrontCamera = !useFrontCamera }) {
                    Text(if (useFrontCamera) "Câmera Frontal" else "Câmera Traseira")
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                // Sem permissão: mostrar botão para solicitar
                !hasCameraPermission.value -> {
                    Text("Permissão de câmera necessária para capturar imagem.", color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Conceder permissão")
                    }
                }

                // Permissão concedida: mostrar preview
                hasCameraPermission.value -> {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        // Preview (AndroidView)
                        AndroidView(
                            factory = { ctx ->
                                val previewView = androidx.camera.view.PreviewView(ctx)
                                previewViewRef.value = previewView

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()

                                        val preview = Preview.Builder().build().also { p ->
                                            p.setSurfaceProvider(previewView.surfaceProvider)
                                        }

                                        val ic = ImageCapture.Builder().build().also { imageCaptureRef.value = it }

                                        val cameraSelector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

                                        try {
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                ic
                                            )
                                        } catch (e: Exception) {
                                            Log.e("CameraScreen", "Erro ao vincular câmera: $e")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("CameraScreen", "Falha ao obter cameraProvider: $e")
                                    }
                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            },
                            modifier = Modifier.fillMaxSize(),
                            update = { previewView ->
                                // Ao trocar o tipo de câmera, rebind manualmente usando o cameraProvider
                                val ctx = previewView.context
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also { p ->
                                        p.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    val ic = imageCaptureRef.value ?: ImageCapture.Builder().build().also { imageCaptureRef.value = it }
                                    val cameraSelector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            ic
                                        )
                                    } catch (e: Exception) {
                                        Log.e("CameraScreen", "Erro ao re-vincular câmera: $e")
                                    }
                                } catch (e: Exception) {
                                    // Se o Future não estiver pronto ainda, será tratado no listener do factory
                                }
                            }
                        )

                        // Overlay de guias de centralização
                        CentralizationGuide(useFrontCamera = useFrontCamera)

                        // Captura e botões em overlay
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .align(Alignment.BottomCenter),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                Button(onClick = {
                                    val ic = imageCaptureRef.value
                                    if (ic == null) {
                                        // fallback: retorna URI simbólica
                                        val cameraType = if (useFrontCamera) "frontal" else "traseira"
                                        onPhotoCaptured("file://foto_ilustrativa.jpg", cameraType)
                                        return@Button
                                    }

                                    val photoFile = createImageFile(context)
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                    ic.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            val savedUri = Uri.fromFile(photoFile).toString()
                                            val cameraType = if (useFrontCamera) "frontal" else "traseira"
                                            // Notificar no thread da UI
                                            (context as? ComponentActivity)?.runOnUiThread {
                                                onPhotoCaptured(savedUri, cameraType)
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Log.e("CameraScreen", "Falha ao salvar imagem: ${exception.message}")
                                            val cameraType = if (useFrontCamera) "frontal" else "traseira"
                                            (context as? ComponentActivity)?.runOnUiThread {
                                                // fallback
                                                onPhotoCaptured("file://foto_ilustrativa.jpg", cameraType)
                                            }
                                        }
                                    })

                                }) {
                                    Text("Capturar")
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Caso padrão
                    Text("Câmera indisponível ou permissão negada.", color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Tentar novamente")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Módulo integrado com CameraX (preview + captura).", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

/**
 * Componente que desenha guias visuais para centralização
 * - Câmera frontal: Oval para rosto
 * - Câmera traseira: Retângulo para corpo inteiro
 */
@Composable
private fun CentralizationGuide(useFrontCamera: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        // Cor das guias (branco semi-transparente)
        val guideColor = Color.White.copy(alpha = 0.7f)
        val strokeWidth = 3f
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)

        if (useFrontCamera) {
            // CÂMERA FRONTAL: Oval para rosto
            val ovalWidth = canvasWidth * 0.55f
            val ovalHeight = canvasHeight * 0.35f
            val ovalTop = centerY - ovalHeight / 2f
            val ovalLeft = centerX - ovalWidth / 2f

            // Desenhar oval tracejado
            drawOval(
                color = guideColor,
                topLeft = Offset(ovalLeft, ovalTop),
                size = Size(ovalWidth, ovalHeight),
                style = Stroke(width = strokeWidth, pathEffect = dashEffect)
            )

            // Linha horizontal central (olhos)
            val eyeLineY = centerY - ovalHeight * 0.1f
            drawLine(
                color = guideColor,
                start = Offset(ovalLeft, eyeLineY),
                end = Offset(ovalLeft + ovalWidth, eyeLineY),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )

            // Linha vertical central (nariz)
            drawLine(
                color = guideColor,
                start = Offset(centerX, ovalTop),
                end = Offset(centerX, ovalTop + ovalHeight),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )

            // Linha horizontal para ombros (abaixo do oval do rosto)
            val shoulderLineY = ovalTop + ovalHeight + 60f
            val shoulderWidth = canvasWidth * 0.65f // Um pouco mais largo que o oval
            val shoulderLeft = centerX - shoulderWidth / 2f
            val shoulderRight = centerX + shoulderWidth / 2f

            // Linha horizontal dos ombros
            drawLine(
                color = guideColor,
                start = Offset(shoulderLeft, shoulderLineY),
                end = Offset(shoulderRight, shoulderLineY),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )

            // Marcadores verticais nas extremidades da linha dos ombros
            drawLine(
                color = guideColor,
                start = Offset(shoulderLeft, shoulderLineY - 20f),
                end = Offset(shoulderLeft, shoulderLineY + 20f),
                strokeWidth = 2.5f
            )
            drawLine(
                color = guideColor,
                start = Offset(shoulderRight, shoulderLineY - 20f),
                end = Offset(shoulderRight, shoulderLineY + 20f),
                strokeWidth = 2.5f
            )

            // Texto de instrução
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 40f
                    textAlign = android.graphics.Paint.Align.CENTER
                    setShadowLayer(8f, 0f, 0f, android.graphics.Color.BLACK)
                }
                canvas.nativeCanvas.drawText(
                    "Centralize rosto e ombros",
                    centerX,
                    ovalTop - 40f,
                    paint
                )
            }

        } else {
            // CÂMERA TRASEIRA: Retângulo para corpo inteiro
            val rectWidth = canvasWidth * 0.6f
            val rectHeight = canvasHeight * 0.75f
            val rectTop = centerY - rectHeight / 2f
            val rectLeft = centerX - rectWidth / 2f

            // Desenhar retângulo tracejado
            drawRect(
                color = guideColor,
                topLeft = Offset(rectLeft, rectTop),
                size = Size(rectWidth, rectHeight),
                style = Stroke(width = strokeWidth, pathEffect = dashEffect)
            )

            // Linha horizontal superior (cabeça)
            val headLineY = rectTop + rectHeight * 0.12f
            drawLine(
                color = guideColor,
                start = Offset(rectLeft, headLineY),
                end = Offset(rectLeft + rectWidth, headLineY),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )

            // Linha horizontal para ombros
            val shoulderLineY = rectTop + rectHeight * 0.22f
            drawLine(
                color = guideColor,
                start = Offset(rectLeft, shoulderLineY),
                end = Offset(rectLeft + rectWidth, shoulderLineY),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )

            // Marcadores laterais para largura dos ombros (aproximadamente 80% da largura do retângulo)
            val shoulderWidth = rectWidth * 0.8f
            val shoulderLeft = centerX - shoulderWidth / 2f
            val shoulderRight = centerX + shoulderWidth / 2f

            // Pequenos traços verticais nas extremidades da linha dos ombros
            drawLine(
                color = guideColor,
                start = Offset(shoulderLeft, shoulderLineY - 15f),
                end = Offset(shoulderLeft, shoulderLineY + 15f),
                strokeWidth = 2f
            )
            drawLine(
                color = guideColor,
                start = Offset(shoulderRight, shoulderLineY - 15f),
                end = Offset(shoulderRight, shoulderLineY + 15f),
                strokeWidth = 2f
            )

            // Linha horizontal central (cintura)
            val waistLineY = rectTop + rectHeight * 0.55f
            drawLine(
                color = guideColor,
                start = Offset(rectLeft, waistLineY),
                end = Offset(rectLeft + rectWidth, waistLineY),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )

            // Linha vertical central
            drawLine(
                color = guideColor,
                start = Offset(centerX, rectTop),
                end = Offset(centerX, rectTop + rectHeight),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )

            // Cantos reforçados para melhor visibilidade
            val cornerLength = 50f
            // Canto superior esquerdo
            drawLine(guideColor, Offset(rectLeft, rectTop), Offset(rectLeft + cornerLength, rectTop), strokeWidth * 2)
            drawLine(guideColor, Offset(rectLeft, rectTop), Offset(rectLeft, rectTop + cornerLength), strokeWidth * 2)
            // Canto superior direito
            drawLine(guideColor, Offset(rectLeft + rectWidth - cornerLength, rectTop), Offset(rectLeft + rectWidth, rectTop), strokeWidth * 2)
            drawLine(guideColor, Offset(rectLeft + rectWidth, rectTop), Offset(rectLeft + rectWidth, rectTop + cornerLength), strokeWidth * 2)
            // Canto inferior esquerdo
            drawLine(guideColor, Offset(rectLeft, rectTop + rectHeight - cornerLength), Offset(rectLeft, rectTop + rectHeight), strokeWidth * 2)
            drawLine(guideColor, Offset(rectLeft, rectTop + rectHeight), Offset(rectLeft + cornerLength, rectTop + rectHeight), strokeWidth * 2)
            // Canto inferior direito
            drawLine(guideColor, Offset(rectLeft + rectWidth, rectTop + rectHeight - cornerLength), Offset(rectLeft + rectWidth, rectTop + rectHeight), strokeWidth * 2)
            drawLine(guideColor, Offset(rectLeft + rectWidth - cornerLength, rectTop + rectHeight), Offset(rectLeft + rectWidth, rectTop + rectHeight), strokeWidth * 2)

            // Texto de instrução
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 40f
                    textAlign = android.graphics.Paint.Align.CENTER
                    setShadowLayer(8f, 0f, 0f, android.graphics.Color.BLACK)
                }
                canvas.nativeCanvas.drawText(
                    "Centralize o corpo",
                    centerX,
                    rectTop - 40f,
                    paint
                )
            }
        }
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File = context.cacheDir
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}
