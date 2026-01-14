package com.example.maptracker.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Delete
    suspend fun deleteRoute(route: RouteEntity)

    @Query("SELECT * FROM routes_table ORDER BY timestamp DESC")
    fun getAllRoutes(): LiveData<List<RouteEntity>>

    @Query("SELECT SUM(timeInMillis) FROM routes_table")
    fun getTotalTime(): LiveData<Long>
}