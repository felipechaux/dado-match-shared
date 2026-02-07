package com.dadomatch.shared.feature.icebreaker.domain.repository

import com.dadomatch.shared.core.util.Resource
import com.dadomatch.shared.feature.icebreaker.domain.model.IcebreakerFeedback

interface IcebreakerRepository {
    suspend fun generateIcebreaker(environment: String, intensity: String, language: String): Resource<String>
    suspend fun submitFeedback(icebreaker: String, feedback: IcebreakerFeedback)
}
