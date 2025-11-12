package com.ruhan.possessao.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruhan.possessao.app.MainViewModel
import com.ruhan.possessao.data.repo.sampleEntities

@Composable
fun TraditionsScreen(vm: MainViewModel, onDone: () -> Unit) {
    val res by vm.result.collectAsState()
    val entity = res?.let { r -> sampleEntities().firstOrNull { it.id == r.entityId } }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("Compêndio solene de expulsão (contexto folclórico)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            Text("Este conteúdo é histórico/folclórico e não prescritivo.", color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))

            if (entity != null) {
                entity.traditions.forEachIndexed { index, step ->
                    Text("${index + 1}. $step", color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                Text("Nenhum candidato selecionado. Volte ao relatório e escolha um candidato.", color = MaterialTheme.colorScheme.onBackground)
            }

            Spacer(Modifier.height(24.dp))
            Button(onClick = onDone) { Text("Concluir") }
        }
    }
}
