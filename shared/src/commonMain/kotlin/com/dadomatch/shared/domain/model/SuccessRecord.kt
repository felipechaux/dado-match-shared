package com.dadomatch.shared.domain.model

data class SuccessRecord(
    val id: String,
    val date: kotlin.time.Instant,
    val environment: String,
    val intensity: String,
    val icebreaker: String,
    val wasSuccessful: Boolean
)
