package com.dadomatch.shared.feature.success.domain.usecase

import com.dadomatch.shared.feature.success.domain.model.SuccessRecord
import com.dadomatch.shared.feature.success.domain.repository.SuccessRepository

class AddSuccessUseCase(private val repository: SuccessRepository) {
    suspend operator fun invoke(record: SuccessRecord) {
        repository.addSuccess(record)
    }
}
