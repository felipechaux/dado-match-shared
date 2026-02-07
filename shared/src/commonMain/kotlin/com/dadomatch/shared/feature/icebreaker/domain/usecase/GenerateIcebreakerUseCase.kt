package com.dadomatch.shared.feature.icebreaker.domain.usecase

import com.dadomatch.shared.feature.icebreaker.domain.repository.IcebreakerRepository
import com.dadomatch.shared.core.util.Resource

class GenerateIcebreakerUseCase(private val repository: IcebreakerRepository) {
    suspend operator fun invoke(environment: String, intensity: String, language: String): Resource<String> {
        return repository.generateIcebreaker(environment, intensity, language)
    }
}
