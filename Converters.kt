package com.ruhan.possessao.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromList(list: List<String>?): String = list?.joinToString("|") ?: ""

    @TypeConverter
    fun toList(data: String?): List<String> =
        if (data.isNullOrBlank()) emptyList() else data.split("|")
}

