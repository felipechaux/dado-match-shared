package com.dadomatch.shared.data.repository

import com.dadomatch.shared.data.local.dao.SuccessDao
import com.dadomatch.shared.data.mapper.toDomain
import com.dadomatch.shared.data.mapper.toEntity
import com.dadomatch.shared.domain.model.SuccessRecord
import com.dadomatch.shared.domain.repository.SuccessRepository
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
