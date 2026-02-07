package com.dadomatch.shared.domain.usecase

import com.dadomatch.shared.domain.repository.IcebreakerRepository
import com.dadomatch.shared.core.Resource

class GenerateIcebreakerUseCase(private val repository: IcebreakerRepository) {
    suspend operator fun invoke(environment: String, intensity: String, language: String): Resource<String> {
        return repository.generateIcebreaker(environment, intensity, language)
    }
}
