package com.example.maptracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    // Comanda pentru a salva o cursă nouă
    @Insert
    suspend fun insertRun(run: RunEntity)

    // Comanda pentru a citi toate cursele (cele mai noi primele)
    @Query("SELECT * FROM run_table ORDER BY timestamp DESC")
    fun getAllRuns(): Flow<List<RunEntity>>
}