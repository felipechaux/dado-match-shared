package com.dadomatch.shared.feature.success.domain.repository

import com.dadomatch.shared.feature.success.domain.model.SuccessRecord
import kotlinx.coroutines.flow.Flow

interface SuccessRepository {
    suspend fun addSuccess(record: SuccessRecord)
    fun getSuccesses(): Flow<List<SuccessRecord>>
}
