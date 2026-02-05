package com.dadomatch.shared.domain.usecase

import com.dadomatch.shared.domain.model.SuccessRecord
import com.dadomatch.shared.domain.repository.SuccessRepository

class AddSuccessUseCase(private val repository: SuccessRepository) {
    suspend operator fun invoke(record: SuccessRecord) {
        repository.addSuccess(record)
    }
}
