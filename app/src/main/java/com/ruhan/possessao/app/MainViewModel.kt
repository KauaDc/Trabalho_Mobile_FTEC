package com.ruhan.possessao.app

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruhan.possessao.ai.AiRepository
import com.ruhan.possessao.data.db.AppDatabase
import com.ruhan.possessao.data.model.EntityRecord
import com.ruhan.possessao.data.repo.EntityRepository
import com.ruhan.possessao.data.repo.sampleEntities
import com.ruhan.possessao.domain.AssessmentEngine
import com.ruhan.possessao.domain.AssessmentInput
import com.ruhan.possessao.domain.AssessmentResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val app: Application, private val db: AppDatabase) : AndroidViewModel(app) {

    private val repo = EntityRepository(db)

    private val _sex = MutableStateFlow<String?>(null)
    val sex: StateFlow<String?> = _sex

    private val _ageGroup = MutableStateFlow("Adulto")
    val ageGroup: StateFlow<String> = _ageGroup

    private val _answers = MutableStateFlow(mapOf<String, Boolean>())
    val answers: StateFlow<Map<String, Boolean>> = _answers

    private val _photoUri = MutableStateFlow<String?>(null)
    val photoUri: StateFlow<String?> = _photoUri

    // Tipo de câmera usada (frontal ou traseira)
    private val _cameraType = MutableStateFlow("traseira")
    val cameraType: StateFlow<String> = _cameraType

    // Expor entidades carregadas
    private val _entities = MutableStateFlow<List<EntityRecord>>(emptyList())
    val entities: StateFlow<List<EntityRecord>> = _entities

    // Lista ranqueada de resultados (top N)
    private val _topResults = MutableStateFlow<List<AssessmentResult>>(emptyList())
    val topResults: StateFlow<List<AssessmentResult>> = _topResults

    // Resultado principal (selecionado)
    private val _result = MutableStateFlow<AssessmentResult?>(null)
    val result: StateFlow<AssessmentResult?> = _result

    // imagem retornada pela API
    private val _resultImageUri = MutableStateFlow<String?>(null)
    val resultImageUri: StateFlow<String?> = _resultImageUri

    // indicador de processamento de imagem
    private val _processingImage = MutableStateFlow(false)
    val processingImage: StateFlow<Boolean> = _processingImage

    init {
        viewModelScope.launch {
            // Seedar banco se vazio (apenas para compatibilidade, mas não é mais usado)
            repo.seedIfEmpty()
            // Forçar uso de sampleEntities() como fonte única para UI e processamento
            _entities.value = sampleEntities()
            Log.d("MainViewModel", "Entidades carregadas (sampleEntities): ${_entities.value.size}")
        }
    }

    fun setSex(value: String?) { _sex.value = value }
    fun setAgeGroup(value: String) { _ageGroup.value = value }
    fun setAnswer(key: String, value: Boolean) {
        _answers.value = _answers.value.toMutableMap().apply { put(key, value) }
    }
    fun setPhoto(uri: String?, cameraType: String = "traseira") {
        _photoUri.value = uri
        _cameraType.value = cameraType
    }

    fun generateResult() {
        viewModelScope.launch {
            // Usar apenas sampleEntities() para garantir que só as entidades ativas sejam usadas
            val combined = sampleEntities()
            Log.d("MainViewModel", "entities from sampleEntities(): ${combined.size}")

            val engine = AssessmentEngine(combined)
            // calcular top list para referência (não exibiremos na UI por padrão)
            val top = engine.assessTopN(
                AssessmentInput(
                    sex = sex.value,
                    ageGroup = ageGroup.value,
                    answers = answers.value,
                    photoUri = photoUri.value
                ),
                3
            )
            _topResults.value = top

            // usar o assess() que aplica a regra de desempate aleatório quando necessário
            val chosen = engine.assess(
                AssessmentInput(
                    sex = sex.value,
                    ageGroup = ageGroup.value,
                    answers = answers.value,
                    photoUri = photoUri.value
                )
            )
            _result.value = chosen

            // Se houver foto, processar localmente
            val photo = photoUri.value
            if (!photo.isNullOrBlank()) {
                _processingImage.value = true
                try {
                    val entityName = chosen.entityId
                    val camType = _cameraType.value

                    // Usar processamento local com sobreposição de imagens
                    val processed = com.ruhan.possessao.ai.LocalImageProcessor.processImage(
                        app, photo, entityName, camType
                    )

                    if (!processed.isNullOrBlank()) {
                        _resultImageUri.value = processed
                    }
                } catch (t: Throwable) {
                    Log.e("MainViewModel", "Falha ao processar imagem: ${t.message}", t)
                } finally {
                    _processingImage.value = false
                }
            }

            // Log para depuração: IDs e confiança (mantido apenas para logs)
            val ids = top.joinToString(",") { it.entityId + "(" + (it.confidence * 100).toInt() + "% )" }
            Log.d("MainViewModel", "generateResult top: $ids | chosen=${chosen.entityId}(${(chosen.confidence*100).toInt()}%) | totalEntitiesUsed=${combined.size}")
        }
    }


    fun selectResult(entityId: String) {
        val sel = _topResults.value.firstOrNull { it.entityId == entityId }
        _result.value = sel ?: AssessmentResult(entityId = entityId, confidence = 0.0, matchedTraits = emptyList())
    }

    // Selecionar entidade manualmente (modo teste)
    fun selectManualEntity(entityId: String) {
        viewModelScope.launch {
            _result.value = AssessmentResult(
                entityId = entityId,
                confidence = 1.0,
                matchedTraits = emptyList()
            )

            // Se houver foto, processar localmente
            val photo = photoUri.value
            if (!photo.isNullOrBlank()) {
                _processingImage.value = true
                try {
                    val camType = _cameraType.value

                    // Usar processamento local com sobreposição de imagens
                    val processed = com.ruhan.possessao.ai.LocalImageProcessor.processImage(
                        app, photo, entityId, camType
                    )

                    if (!processed.isNullOrBlank()) {
                        _resultImageUri.value = processed
                    }
                } catch (t: Throwable) {
                    Log.e("MainViewModel", "Falha ao processar imagem: ${t.message}", t)
                } finally {
                    _processingImage.value = false
                }
            }
        }
    }

    fun resetAll() {
        _sex.value = null
        _ageGroup.value = "Adulto"
        _answers.value = emptyMap()
        _photoUri.value = null
        _cameraType.value = "traseira"
        _result.value = null
        _topResults.value = emptyList()
        _resultImageUri.value = null
        _processingImage.value = false
    }
}
