package com.ruhan.possessao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ruhan.possessao.app.MainViewModel

@Composable
fun ResultScreen(
    vm: MainViewModel,
    onSeeTraditions: () -> Unit,
    onRestart: () -> Unit
) {
    val result by vm.result.collectAsState()
    val originalPhoto by vm.photoUri.collectAsState()
    val processedPhoto by vm.resultImageUri.collectAsState()
    val processing by vm.processingImage.collectAsState()
    val entities by vm.entities.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Text("Relatório místico de identificação", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))

            if (result != null) {
                val entity = entities.firstOrNull { it.id == result!!.entityId }

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(entity?.name ?: result!!.entityId, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))

                        if (processing) {
                            Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Processando imagem...", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(12.dp))
                        } else {
                            val showImage = processedPhoto ?: originalPhoto
                            if (!showImage.isNullOrBlank()) {
                                AsyncImage(
                                    model = showImage,
                                    contentDescription = "Imagem processada",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }

                        Text(entity?.description ?: "Sem descrição disponível.", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onSeeTraditions) { Text("Ver passos solenes") }
                    OutlinedButton(onClick = onRestart) { Text("Reiniciar") }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma entidade identificada. Tente ajustar as respostas.", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
