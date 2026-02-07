package com.dadomatch.shared.feature.icebreaker.data.repository

import com.dadomatch.shared.feature.icebreaker.domain.repository.IcebreakerRepository
import com.dadomatch.shared.core.util.Resource
import com.dadomatch.shared.feature.icebreaker.data.remote.GeminiService
import com.dadomatch.shared.feature.icebreaker.domain.model.IcebreakerFeedback

class IcebreakerRepositoryImpl(
    private val geminiService: GeminiService
) : IcebreakerRepository {
    override suspend fun generateIcebreaker(environment: String, intensity: String, language: String): Resource<String> {
        return geminiService.generateIcebreaker(environment, intensity, language)
    }

    override suspend fun submitFeedback(icebreaker: String, feedback: IcebreakerFeedback) {
        // For now, we just log it. In a real app, this would send it to an analytics service or database.
        println("Feedback for '$icebreaker': $feedback")
    }
}
