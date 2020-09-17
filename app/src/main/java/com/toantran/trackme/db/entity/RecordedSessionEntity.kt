package com.toantran.trackme.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName ="recorded_session_table")
data class RecordedSessionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val averageSpeed: Float,
    val totalDistance: Double,
    val totalDuration: Int,
    val mapImagePath: String

)