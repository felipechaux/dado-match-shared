package com.dadomatch.shared.feature.success.data.mapper

import com.dadomatch.shared.feature.success.data.local.entity.SuccessEntity
import com.dadomatch.shared.feature.success.domain.model.SuccessRecord

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
