package com.ruhan.possessao.domain

data class AssessmentInput(
    val sex: String?,          // "Masculino", "Feminino", "Não informar"
    val ageGroup: String,      // "Criança", "Adolescente", "Adulto", "Idoso"
    val answers: Map<String, Boolean>,
    val photoUri: String?
)

data class AssessmentResult(
    val entityId: String,
    val confidence: Double,
    val matchedTraits: List<String>
)

