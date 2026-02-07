package com.dadomatch.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "success_records")
data class SuccessEntity(
    @PrimaryKey
    val id: String,
    val dateMillis: Long,
    val environment: String,
    val intensity: String,
    val icebreaker: String,
    val wasSuccessful: Boolean
)
