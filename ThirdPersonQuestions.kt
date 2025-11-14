package com.ruhan.possessao.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruhan.possessao.app.MainViewModel

@Composable
fun ThirdPersonQuestionsScreen(
    vm: MainViewModel,
    onNext: () -> Unit
) {
    // Reaproveita o estado centralizado no ViewModel
    val answersState by vm.answers.collectAsState()

    val allQuestionsThirdPerson = listOf(
        "xenoglossia" to "A pessoa começa a falar línguas que nunca aprendeu?",
        "aversion_symbols" to "A pessoa sente algum desconforto perto de símbolos religiosos?",
        "voice_shift" to "A voz da pessoa muda de um jeito estranho, sem explicação?",
        "somnambulism" to "A pessoa anda dormindo com frequência e conta coisas esquisitas depois?",
        "mood_swings" to "A pessoa muda de humor do nada, sem motivo claro?",
        "temperature_shift" to "A temperatura do corpo da pessoa altera sem explicação?",
        "object_movement" to "Os objetos perto da pessoa se mexem sozinhos, ou ela tem lapsos de raiva arremessando materiais?",
        "memory_gaps" to "A pessoa tem apagões ou esquece o que fez durante comportamentos estranhos?",
        "shadow_presence" to "Alguém já disse que viu sombras ou vultos perto da pessoa?",
        "mirror_discomfort" to "A pessoa evita se olhar no espelho ou parece incomodada com o próprio reflexo?",
        "unusual_strength" to "A pessoa já mostrou uma força fora do comum em alguns momentos?",
        "animal_reaction" to "Animais ficam agitados, agressivos ou desconfortáveis quando estão perto da pessoa?",
        "time_distortion" to "A pessoa sente que o tempo passa diferente durante certos episódios?",
        "persistent_whispers" to "A pessoa ouve sussurros ou vozes mesmo quando não tem ninguém por perto?",
        "symbolic_drawings" to "A pessoa faz desenhos ou símbolos repetidos sem saber o porquê?",
        "unexplained_fatigue" to "A pessoa sente cansaço além do normal?"
    )

    fun getRandomQuestions(quantity: Int = 7): List<Pair<String, String>> =
        allQuestionsThirdPerson.shuffled().take(quantity)

    val questions = remember { getRandomQuestions() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "Questionário de observação (terceira pessoa)",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(12.dp))

            questions.forEach { (key, label) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                        Switch(
                            checked = answersState[key] == true,
                            onCheckedChange = { vm.setAnswer(key, it) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Continuar") }
        }
    }
}
