package com.dadomatch.shared.domain.repository

import com.dadomatch.shared.core.Resource
import com.dadomatch.shared.domain.model.IcebreakerFeedback

interface IcebreakerRepository {
    suspend fun generateIcebreaker(environment: String, intensity: String, language: String): Resource<String>
    suspend fun submitFeedback(icebreaker: String, feedback: IcebreakerFeedback)
}
