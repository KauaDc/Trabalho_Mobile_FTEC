
package com.ruhan.possessao.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruhan.possessao.app.MainViewModel

@Composable
fun QuestionTargetScreen(
    vm: MainViewModel,
    toFirstPersonQuestions: () -> Unit,
    toThirdPersonQuestions: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Quem está respondendo?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Escolha para quem é o questionário. Isso ajusta a forma das perguntas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    vm.setAnsweringFor("self")
                    toFirstPersonQuestions()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Estou respondendo por mim")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    vm.setAnsweringFor("other")
                    toThirdPersonQuestions()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Estou respondendo por outra pessoa")
            }
        }
    }
}
