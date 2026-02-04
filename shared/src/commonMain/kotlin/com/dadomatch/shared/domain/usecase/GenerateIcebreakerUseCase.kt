package com.dadomatch.shared.domain.usecase

import com.dadomatch.shared.core.Resource
import com.dadomatch.shared.domain.repository.IcebreakerRepository

class GenerateIcebreakerUseCase(private val repository: IcebreakerRepository) {
    suspend operator fun invoke(environment: String, intensity: String): Resource<String> {
        return repository.generateIcebreaker(environment, intensity)
    }
}
