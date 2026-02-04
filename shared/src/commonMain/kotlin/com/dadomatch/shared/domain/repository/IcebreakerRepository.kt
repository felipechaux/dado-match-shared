package com.dadomatch.shared.domain.repository

import com.dadomatch.shared.core.Resource

interface IcebreakerRepository {
    suspend fun generateIcebreaker(environment: String, intensity: String): Resource<String>
}
