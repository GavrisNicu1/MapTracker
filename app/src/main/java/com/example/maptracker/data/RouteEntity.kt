package com.example.maptracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes_table")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,             // ID unic
    val timestamp: Long,         // Când a fost făcut traseul
    val timeInMillis: Long,      // Durata
    val distanceInMeters: Float, // Distanța
    val imgUrl: String = "",     // (Opțional) Screenshot hartă

    // Coordonatele vor fi salvate ca un text lung (JSON)
    val pointsJson: String
)