package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "status_update_logs")
data class StatusUpdateLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val applicationUid: Int,
    val title: String,
    val location: String,
    val timeAgo: String,
    val isCompleted: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
