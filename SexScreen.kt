package com.ruhan.possessao.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruhan.possessao.app.MainViewModel

@Composable
fun SexScreen(vm: MainViewModel, onNext: () -> Unit) {
    var selected by remember { mutableStateOf(vm.sex.value) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("Registrar sexo da pessoa", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    val options = listOf("Feminino", "Masculino", "Não informar")
                    options.forEach { opt ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { selected = opt },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(opt, color = MaterialTheme.colorScheme.onBackground)
                            RadioButton(
                                selected = selected == opt,
                                onClick = { selected = opt },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                        Divider()
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.setSex(selected); onNext() },
                enabled = selected != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text("Avançar")
            }
        }
    }
}
