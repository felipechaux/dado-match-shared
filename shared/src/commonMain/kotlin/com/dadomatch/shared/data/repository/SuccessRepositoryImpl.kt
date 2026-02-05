package com.dadomatch.shared.data.repository

import com.dadomatch.shared.domain.model.SuccessRecord
import com.dadomatch.shared.domain.repository.SuccessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SuccessRepositoryImpl : SuccessRepository {
    private val _successes = MutableStateFlow(createMockData())
    
    override suspend fun addSuccess(record: SuccessRecord) {
        _successes.update { it + record }
    }

    override fun getSuccesses(): Flow<List<SuccessRecord>> {
        return _successes.asStateFlow()
    }

    private fun createMockData(): List<SuccessRecord> {
        val now = kotlin.time.Clock.System.now()

        return listOf(
            SuccessRecord("1", now.minus(5.toDuration(DurationUnit.DAYS)), "Fiesta", "Gracioso", "Mocked 1", true),
            SuccessRecord("3", now.minus(3.toDuration(DurationUnit.DAYS)), "Café", "Romántico", "Mocked 3", false),
            SuccessRecord("5", now.minus(1.toDuration(DurationUnit.DAYS)), "Fiesta", "Romántico", "Mocked 5", true)
        )
    }

}
