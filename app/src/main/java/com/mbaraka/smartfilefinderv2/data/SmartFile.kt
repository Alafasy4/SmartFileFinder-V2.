package com.mbaraka.smartfilefinderv2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_files")
data class SmartFile(
    @PrimaryKey val path: String,
    val fileName: String,
    val extension: String,
    val size: Long,
    val modifiedDate: Long,
    val mimeType: String,
    val category: String,
    // Intelligence fields
    val accessCount: Int = 0,
    val lastOpened: Long = 0,
    val score: Float = 0f // Used for recommendation logic
)
