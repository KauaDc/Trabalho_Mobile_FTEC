package com.ruhan.possessao.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruhan.possessao.app.MainViewModel

@Composable
fun QuestionsScreen(
    vm: MainViewModel,
    onPhoto: () -> Unit,
    onGenerate: () -> Unit,
    onManualSelect: () -> Unit = {}
) {
    // Usar o estado centralizado no ViewModel para persistir respostas entre telas
    val answersState by vm.answers.collectAsState()

    val allQuestionsFirstPerson = listOf(
        "xenoglossia" to "Começa a falar línguas que nunca aprendeu",
        "aversion_symbols" to "Sente algum desconforto perto de símbolos religiosos",
        "voice_shift" to "A voz muda de um jeito estranho, sem explicação",
        "somnambulism" to "Anda dormindo com frequência e conta coisas esquisitas depois",
        "mood_swings" to "Muda de humor do nada, sem motivo claro",
        "temperature_shift" to "A temperatura do corpo altera sem explicação",
        "object_movement" to "Objetos próximos se mexem sozinhos, ou há lapsos de raiva com arremesso de materiais",
        "memory_gaps" to "Tem apagões ou esquece o que fez durante comportamentos estranhos",
        "shadow_presence" to "Alguém já comentou ter visto sombras ou vultos por perto",
        "mirror_discomfort" to "Evita se olhar no espelho ou sente incômodo com o próprio reflexo",
        "unusual_strength" to "Já demonstrou força fora do comum em alguns momentos",
        "animal_reaction" to "Animais ficam agitados, agressivos ou desconfortáveis quando estão por perto",
        "time_distortion" to "A percepção do tempo muda durante certos episódios",
        "persistent_whispers" to "Ouve sussurros ou vozes mesmo quando não há ninguém por perto",
        "symbolic_drawings" to "Faz desenhos ou símbolos repetidos sem saber o motivo",
        "unexplained_fatigue" to "Sente cansaço além do normal"
    )

    fun getRandomQuestions(quantity: Int = 7): List<Pair<String, String>> {
        return allQuestionsFirstPerson.shuffled().take(quantity)
    }

// Exemplo de uso:
    val questions = getRandomQuestions()

//    val questions = listOf(
//        "xenoglossia" to "A pessoa fala idiomas desconhecidos espontaneamente?",
//        "aversion_symbols" to "Há desconforto intenso próximo de símbolos religiosos?",
//        "voice_shift" to "A voz muda de forma incomum sem explicação conhecida?",
//        "somnambulism" to "Há sonambulismo frequente com relatos estranhos?",
//        "mood_swings" to "Mudanças abruptas de humor são percebidas?"
//    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Questionário solene de observação", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))

            questions.forEach { (key, label) ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                        Switch(
                            checked = answersState[key] == true,
                            onCheckedChange = {
                                vm.setAnswer(key, it)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Registro imagético (opcional):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onPhoto) { Text("Adicionar foto") }
                Button(onClick = onGenerate, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Gerar resultado") }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Botão para modo teste
            Text("Modo Teste:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onManualSelect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Escolher Entidade Manualmente")
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
