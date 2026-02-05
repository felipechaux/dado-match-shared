package com.dadomatch.shared.domain.usecase

import IcebreakerRepository
import com.dadomatch.shared.domain.model.IcebreakerFeedback

class SubmitFeedbackUseCase(private val repository: IcebreakerRepository) {
    suspend operator fun invoke(icebreaker: String, feedback: IcebreakerFeedback) {
        repository.submitFeedback(icebreaker, feedback)
    }
}
