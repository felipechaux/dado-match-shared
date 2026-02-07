package com.dadomatch.shared.data.mapper

import com.dadomatch.shared.data.local.entity.SuccessEntity
import com.dadomatch.shared.domain.model.SuccessRecord
import kotlin.time.Duration.Companion.milliseconds

/**
 * Converts a Room entity to a domain model
 */
fun SuccessEntity.toDomain(): SuccessRecord {
    return SuccessRecord(
        id = id,
        date = kotlin.time.Instant.fromEpochMilliseconds(dateMillis),
        environment = environment,
        intensity = intensity,
        icebreaker = icebreaker,
        wasSuccessful = wasSuccessful
    )
}

/**
 * Converts a domain model to a Room entity
 */
fun SuccessRecord.toEntity(): SuccessEntity {
    return SuccessEntity(
        id = id,
        dateMillis = date.toEpochMilliseconds(),
        environment = environment,
        intensity = intensity,
        icebreaker = icebreaker,
        wasSuccessful = wasSuccessful
    )
}
