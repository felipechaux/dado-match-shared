package com.dadomatch.shared.feature.icebreaker.domain.usecase

import com.dadomatch.shared.feature.icebreaker.domain.repository.IcebreakerRepository
import com.dadomatch.shared.feature.icebreaker.domain.model.IcebreakerFeedback

class SubmitFeedbackUseCase(private val repository: IcebreakerRepository) {
    suspend operator fun invoke(icebreaker: String, feedback: IcebreakerFeedback) {
        repository.submitFeedback(icebreaker, feedback)
    }
}
