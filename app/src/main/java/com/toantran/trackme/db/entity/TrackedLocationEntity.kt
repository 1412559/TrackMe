package com.toantran.trackme.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName ="tracked_location_info_table")
data class TrackedLocationEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val distance: Float,
    val duration: Float,
    val currentTime: Date

)