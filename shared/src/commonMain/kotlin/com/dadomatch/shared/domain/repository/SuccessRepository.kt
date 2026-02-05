package com.dadomatch.shared.domain.repository

import com.dadomatch.shared.domain.model.SuccessRecord
import kotlinx.coroutines.flow.Flow

interface SuccessRepository {
    suspend fun addSuccess(record: SuccessRecord)
    fun getSuccesses(): Flow<List<SuccessRecord>>
}
