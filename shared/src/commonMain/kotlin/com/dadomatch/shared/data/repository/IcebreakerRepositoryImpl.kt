package com.dadomatch.shared.data.repository

import com.dadomatch.shared.core.Resource
import com.dadomatch.shared.data.remote.GeminiService
import com.dadomatch.shared.domain.repository.IcebreakerRepository

class IcebreakerRepositoryImpl(
    private val geminiService: GeminiService
) : IcebreakerRepository {
    override suspend fun generateIcebreaker(environment: String, intensity: String): Resource<String> {
        return geminiService.generateIcebreaker(environment, intensity)
    }
}
