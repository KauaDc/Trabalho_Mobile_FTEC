package com.ruhan.possessao.data.repo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruhan.possessao.data.model.EntityRecord

@Dao
interface EntityDao {
    @Query("SELECT * FROM entities")
    suspend fun getAll(): List<EntityRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<EntityRecord>)

    @Query("DELETE FROM entities")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM entities")
    suspend fun count(): Int
}
