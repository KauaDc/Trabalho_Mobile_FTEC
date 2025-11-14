package com.ruhan.possessao.data.repo

import com.ruhan.possessao.data.db.AppDatabase
import com.ruhan.possessao.data.model.EntityRecord

class EntityRepository(private val db: AppDatabase) {
    suspend fun getAll(): List<EntityRecord> = db.entityDao().getAll()
    suspend fun seedIfEmpty() {
        val current = db.entityDao().getAll()
        if (current.isEmpty()) {
            db.entityDao().insertAll(sampleEntities())
        }
    }

    suspend fun count(): Int = db.entityDao().count()

    suspend fun replaceWithSampleIfBelow(minCount: Int) {
        val c = db.entityDao().count()
        if (c < minCount) {
            db.entityDao().deleteAll()
            db.entityDao().insertAll(sampleEntities())
        }
    }
}

fun sampleEntities(): List<EntityRecord> =
    listOf(
        EntityRecord(
            id = "pazuzu",
            name = "Pazuzu",
            culture = "Mesopotâmica",
            traits = listOf("somnambulism", "mood_swings", "temperature_shift"),
            //manifestationLevel = "Moderado",
            description = "Espírito dos ventos do sudoeste, híbrido e protetor contra forças malignas como Lamashtu, porém de humor instável e presença noturna.",
            traditions = listOf(
                "Etapa I: Abrir janelas ou portas por alguns minutos, permitindo que o ar circule livremente.",
                "Etapa II: Caminhar ao ar livre e respirar fundo, simbolizando a libertação dos ventos.",
                "Etapa III: Deixar uma pequena oferenda simbólica ao vento, como uma flor ou folha seca."
            ),
            references = listOf("Britannica – Pazuzu", "Dictionnaire Infernal (Collin de Plancy, 1818)")
        ),

        EntityRecord(
            id = "lamashtu",
            name = "Lamashtu",
            culture = "Mesopotâmica",
            traits = listOf("mood_swings", "aversion_symbols", "unexplained_fatigue"),
            //manifestationLevel = "Alto",
            description = "Figura feminina monstruosa, metade leoa e metade humana, associada ao medo e às mudanças emocionais das noites antigas.",
            traditions = listOf(
                "Etapa I: Acender uma vela e mentalizar proteção e equilíbrio.",
                "Etapa II: Colocar um copo d’água ao lado da cama antes de dormir.",
                "Etapa III: Descartar essa água pela manhã, simbolizando o fim da influência de Lamashtu."
            ),
            references = listOf("Met Museum – Amuletos de Lamashtu", "Harvard Library Bulletin – Demonologia Mesopotâmica"),
            affectedGenders = listOf("Feminino"),
            affectedAgeGroups = listOf("Adulto", "Idoso")
        ),

        EntityRecord(
            id = "legiao",
            name = "Legião",
            culture = "Cristã",
            traits = listOf("voice_shift", "aversion_symbols", "mood_swings", "persistent_whispers"),
          //  //manifestationLevel = "Crítico",
            description = "Entidade coletiva mencionada no Novo Testamento — 'somos muitos' — símbolo de vozes internas conflitantes e desordem emocional.",
            traditions = listOf(
                "Etapa I: Fazer silêncio por alguns minutos e respirar profundamente.",
                "Etapa II: Falar em voz alta uma frase afirmativa: 'Eu sou um só, em equilíbrio'.",
                "Etapa III: Lavar as mãos e rosto, simbolizando purificação e retorno à unidade."
            ),
            references = listOf("Evangelho de Marcos 5:1–20", "Comentário Bíblico NCEC – Legião e simbolismo coletivo"),
            affectedGenders = listOf("Masculino")
        ),

        EntityRecord(
            id = "beelzebub",
            name = "Beelzebub",
            culture = "Cristã Ocidental",
            traits = listOf("aversion_symbols", "mood_swings", "object_movement", "unusual_strength"),
            //manifestationLevel = "Alto",
            description = "Figura demoníaca clássica, associada à decadência e à gula, representando a corrupção e o orgulho humanos.",
            traditions = listOf(
                "Etapa I: Deixar entrar a luz solar por alguns minutos.",
                "Etapa II: Fazer uma limpeza rápida no ambiente.",
                "Etapa III: Acender um incenso ou vela, agradecendo pela harmonia restabelecida."
            ),
            references = listOf("Dictionnaire Infernal (1818)", "Wikipedia – Beelzebub")
        ),

        EntityRecord(
            id = "aka_oni",
            name = "Aka Oni",
            culture = "Japonesa",
            traits = listOf("mood_swings", "aversion_symbols", "temperature_shift", "unusual_strength"),
            //manifestationLevel = "Moderado",
            description = "Oni vermelho do folclore japonês, símbolo da raiva e da impulsividade. Representa emoções que explodem como fogo.",
            traditions = listOf(
                "Etapa I: Gritar em um local aberto, liberando simbolicamente a raiva.",
                "Etapa II: Fazer três respirações lentas, visualizando o calor se dissipando.",
                "Etapa III: Beber um copo de água fria para restaurar a calma."
            ),
            references = listOf("Reider, Noriko T. – Japanese Demon Lore", "Kojiki – Mitos do Japão Antigo"),
            affectedGenders = listOf("Masculino")
        ),

        EntityRecord(
            id = "ao_oni",
            name = "Ao Oni",
            culture = "Japonesa",
            traits = listOf("somnambulism", "mood_swings", "shadow_presence", "time_distortion"),
            //manifestationLevel = "Baixo",
            description = "Oni azul, associado à tristeza e ao arrependimento. Move-se silenciosamente durante a noite, confundindo os sonhadores.",
            traditions = listOf(
                "Etapa I: Acender uma luz suave e escrever algo positivo no papel.",
                "Etapa II: Ler em voz alta uma lembrança boa do passado.",
                "Etapa III: Guardar o papel em local tranquilo, simbolizando o descanso do espírito."
            ),
            references = listOf("Yōkai Daizukai – Mizuki Shigeru", "Festival Setsubun – registros culturais"),
            affectedAgeGroups = listOf("Adolescente")
        ),

        EntityRecord(
            id = "namahage",
            name = "Namahage",
            culture = "Japonesa (Akita)",
            traits = listOf("voice_shift", "aversion_symbols", "animal_reaction"),
            //manifestationLevel = "Leve",
            description = "Espírito mascarado que visita casas durante o inverno para assustar crianças preguiçosas. Na verdade, é um protetor ritualístico.",
            traditions = listOf(
                "Etapa I: Tocar um sino ou fazer barulho, como nas festas de inverno.",
                "Etapa II: Limpar a entrada da casa, simbolizando boas-vindas.",
                "Etapa III: Agradecer em voz alta pelo ano que passou."
            ),
            references = listOf("Museu Folclórico de Akita", "Festival Namahage Sedo Matsuri"),
            affectedAgeGroups = listOf("Criança")
        ),

        EntityRecord(
            id = "ifrit",
            name = "Ifrit",
            culture = "Islâmica",
            traits = listOf("xenoglossia", "mood_swings", "temperature_shift", "unusual_strength"),
            //manifestationLevel = "Crítico",
            description = "Jinn de fogo puro, descrito como poderoso e orgulhoso. Simboliza o controle das paixões e do orgulho humano.",
            traditions = listOf(
                "Etapa I: Ficar em silêncio por um minuto observando uma chama.",
                "Etapa II: Respirar lentamente até sentir calma interior.",
                "Etapa III: Soprar a chama com respeito, representando o domínio sobre o fogo."
            ),
            references = listOf("Al-Jahiz – Kitab al-Hayawan", "Oxford Islamic Studies – Ifrit"),
            affectedAgeGroups = listOf("Adulto")
        ),

        EntityRecord(
            id = "marid",
            name = "Marid",
            culture = "Islâmica",
            traits = listOf("voice_shift", "mood_swings", "memory_gaps", "time_distortion"),
            //manifestationLevel = "Moderado",
            description = "Jinn das águas profundas e das tempestades. Representa emoções presas e desejos não expressos.",
            traditions = listOf(
                "Etapa I: Lavar as mãos e o rosto com água fria, em silêncio.",
                "Etapa II: Ficar próximo a uma fonte ou rio por alguns minutos.",
                "Etapa III: Mentalizar o som da água levando embora preocupações."
            ),
            references = listOf("Kitab al-Bulhan (séc. XIV)", "Qur'an Surata 55 – Ar-Rahman"),
            affectedAgeGroups = listOf("Adulto")
        ),

        EntityRecord(
            id = "ghul",
            name = "Ghul",
            culture = "Islâmica",
            traits = listOf("somnambulism", "aversion_symbols", "shadow_presence", "object_movement"),
            //manifestationLevel = "Alto",
            description = "Jinn associado a desertos e cemitérios. Gosta de se disfarçar e confundir viajantes. Símbolo do medo do desconhecido.",
            traditions = listOf(
                "Etapa I: Caminhar alguns passos em linha reta, observando atentamente o caminho.",
                "Etapa II: Falar uma frase de coragem em voz alta.",
                "Etapa III: Acender uma lanterna para simbolizar o retorno à clareza."
            ),
            references = listOf("Al-Qazwini – Aja'ib al-Makhluqat", "Encyclopaedia of Islam – Ghul"),
            affectedAgeGroups = listOf("Adulto")
        ),

        EntityRecord(
            id = "silat",
            name = "Si’lat",
            culture = "Islâmica",
            traits = listOf("xenoglossia", "mood_swings", "mirror_discomfort", "symbolic_drawings"),
            //manifestationLevel = "Moderado",
            description = "Jinn sedutor e mutável, famoso por enganar viajantes com aparências belas. Representa a ilusão e o autoengano.",
            traditions = listOf(
                "Etapa I: Olhar-se no espelho e dizer o próprio nome três vezes.",
                "Etapa II: Respirar fundo e sorrir, reconhecendo quem se é de verdade.",
                "Etapa III: Agradecer em voz alta pela autenticidade recuperada."
            ),
            references = listOf("Al-Jahiz – Kitab al-Hayawan", "Oxford Dictionary of Islam – Si’lat"),
            affectedGenders = listOf("Feminino"),
            affectedAgeGroups = listOf("Adulto")
        )

    )
