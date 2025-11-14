package com.ruhan.possessao.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entities")
data class EntityRecord(
    @PrimaryKey val id: String,
    val name: String,
    val culture: String,
    val traits: List<String>,     // campos compostos usando converter
    val description: String,
    val traditions: List<String>, // passos "sérios" porém divertidos
    val references: List<String>, // textos curtos de referência

    // Novos campos opcionais para indicar preferência por gênero(es) e faixa(s) etárias
    // Se as listas estiverem vazias, significa "qualquer"
    val affectedGenders: List<String> = emptyList(),      // ex.: listOf("Masculino"), listOf("Feminino"), vazio = qualquer
    val affectedAgeGroups: List<String> = emptyList()     // ex.: listOf("Criança"), listOf("Adulto"), vazio = qualquer
)
