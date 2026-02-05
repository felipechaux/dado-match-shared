package com.dadomatch.shared.domain.usecase

import com.dadomatch.shared.domain.model.SuccessRecord
import com.dadomatch.shared.domain.repository.SuccessRepository
import kotlinx.coroutines.flow.Flow

class GetSuccessesUseCase(private val repository: SuccessRepository) {
    operator fun invoke(): Flow<List<SuccessRecord>> {
        return repository.getSuccesses()
    }
}
