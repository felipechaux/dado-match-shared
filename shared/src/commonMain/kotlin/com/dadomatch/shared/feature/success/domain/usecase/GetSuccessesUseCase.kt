package com.dadomatch.shared.feature.success.domain.usecase

import com.dadomatch.shared.feature.success.domain.model.SuccessRecord
import com.dadomatch.shared.feature.success.domain.repository.SuccessRepository
import kotlinx.coroutines.flow.Flow

class GetSuccessesUseCase(private val repository: SuccessRepository) {
    operator fun invoke(): Flow<List<SuccessRecord>> {
        return repository.getSuccesses()
    }
}
