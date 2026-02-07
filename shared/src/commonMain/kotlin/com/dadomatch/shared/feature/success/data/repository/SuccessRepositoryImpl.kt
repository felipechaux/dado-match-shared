package com.dadomatch.shared.feature.success.data.repository

import com.dadomatch.shared.feature.success.data.local.dao.SuccessDao
import com.dadomatch.shared.feature.success.data.mapper.toDomain
import com.dadomatch.shared.feature.success.data.mapper.toEntity
import com.dadomatch.shared.feature.success.domain.model.SuccessRecord
import com.dadomatch.shared.feature.success.domain.repository.SuccessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SuccessRepositoryImpl(
    private val successDao: SuccessDao
) : SuccessRepository {
    
    override suspend fun addSuccess(record: SuccessRecord) {
        successDao.insertSuccess(record.toEntity())
    }

    override fun getSuccesses(): Flow<List<SuccessRecord>> {
        return successDao.getAllSuccesses()
            .map { entities -> entities.map { it.toDomain() } }
    }
}
