package com.ruhan.possessao.domain

import android.util.Log
import com.ruhan.possessao.data.model.EntityRecord
import kotlin.random.Random

class AssessmentEngine(private val entities: List<EntityRecord>) {

    fun assess(input: AssessmentInput): AssessmentResult {
        // Obter top 3 candidatos (ou menos) com confiança calculada
        val topList = assessTopN(input, 3)
        if (topList.isEmpty()) return AssessmentResult(entityId = "", confidence = 0.0, matchedTraits = emptyList())

        // Encontrar maior confiança
        val maxConfidence = topList.maxOf { it.confidence }
        // Filtrar candidatos com a maior confiança
        val bestCandidates = topList.filter { it.confidence == maxConfidence }

        val chosen = if (bestCandidates.size == 1) {
            bestCandidates.first()
        } else {
            // Escolha aleatória entre os empatados
            bestCandidates[Random.nextInt(bestCandidates.size)]
        }

        Log.d("AssessmentEngine", "Escolhido: ${chosen.entityId} com confiança ${chosen.confidence}")
        return chosen
    }

    fun assessTopN(input: AssessmentInput, n: Int = 3): List<AssessmentResult> {
        if (entities.isEmpty()) return emptyList()

        // Calcular frequência de cada trait entre as entidades (para dar peso a traços raros)
        val freq = mutableMapOf<String, Int>()
        for (e in entities) {
            for (t in e.traits) {
                freq[t] = (freq[t] ?: 0) + 1
            }
        }

        data class ScoreInfo(
            val id: String,
            val score: Double,
            val matchCount: Int,
            val matchRatio: Double,
            val matched: List<String>,
            val rarityBonus: Double,
            val genderFactor: Double,
            val ageFactor: Double
        )

        val scored = entities.map { e ->
            val traitCount = e.traits.size.coerceAtLeast(1)
            val matched = e.traits.filter { t -> input.answers[t] == true }
            val matchCount = matched.size
            val matchRatio = matchCount.toDouble() / traitCount

            val rarityBonus = matched.sumOf { t ->
                val f = freq[t] ?: 1
                1.0 / f.toDouble()
            }

            // Bônus por idade antiga (trait legacy)
            val legacyAgeBonus = if (input.ageGroup == "Criança" && e.traits.contains("affects_youth")) 1.0 else 0.0

            // Fatores de gênero/faixa etária declarados na entidade
            val inputSex = input.sex?.takeIf { it.isNotBlank() && !it.equals("Não informar", true) }

            val genderFactor = when {
                e.affectedGenders.isEmpty() -> 0.0
                inputSex == null -> 0.0 // sem informação, não penaliza
                e.affectedGenders.any { it.equals(inputSex, true) } -> 10.0
                else -> -8.0
            }

            val inputAge = input.ageGroup
            val ageFactor = when {
                e.affectedAgeGroups.isEmpty() -> 0.0
                inputAge.isBlank() -> 0.0
                e.affectedAgeGroups.any { it.equals(inputAge, true) } -> 10.0
                else -> -6.0
            }

            val score = matchRatio * 100.0 + matchCount * 6.0 + rarityBonus * 12.0 + legacyAgeBonus * 4.0 + genderFactor + ageFactor

            // Log detalhado por entidade para depuração
            Log.d(
                "AssessmentEngine",
                "Entity=${e.id} matched=${matched.size} matchedTraits=${matched} matchRatio=${String.format(java.util.Locale.US, "%.2f", matchRatio)} rarityBonus=${String.format(java.util.Locale.US, "%.2f", rarityBonus)} genderFactor=${String.format(java.util.Locale.US, "%.2f", genderFactor)} ageFactor=${String.format(java.util.Locale.US, "%.2f", ageFactor)} score=${String.format(java.util.Locale.US, "%.2f", score)}"
            )

            ScoreInfo(e.id, score, matchCount, matchRatio, matched, rarityBonus, genderFactor, ageFactor)
        }

        val ranked = scored.sortedWith(
            compareByDescending<ScoreInfo> { it.score }
                .thenByDescending { it.matchCount }
                .thenByDescending { it.matchRatio }
        )

        // Filtrar apenas com matchCount > 0 para mostrar somente candidatos relevantes
        val positive = ranked.filter { it.matchCount > 0 }
        val chosenList = if (positive.isNotEmpty()) positive.take(n) else ranked.take(n)

        if (chosenList.isNotEmpty()) {
            val rankLogPos = chosenList.joinToString(" | ") { r -> "${r.id}:score=${String.format(java.util.Locale.US, "%.2f", r.score)} matches=${r.matchCount} gf=${String.format(java.util.Locale.US, "%.2f", r.genderFactor)} af=${String.format(java.util.Locale.US, "%.2f", r.ageFactor)}" }
            Log.d("AssessmentEngine", "Ranking (selected list): $rankLogPos")
        }

        return chosenList.map { s ->
            val confidence = if (s.matchCount == 0) 0.18 else {
                val c = 0.35 + (s.matchRatio * 0.55) + (s.matchCount * 0.03)
                c.coerceIn(0.35, 0.95)
            }
            AssessmentResult(entityId = s.id, confidence = confidence, matchedTraits = s.matched)
        }
    }
}
