package com.example.maptracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_table")
data class RunEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,  // Când a avut loc alergarea (data și ora)
    val distanceKm: Float // Câți km ai parcurs
)