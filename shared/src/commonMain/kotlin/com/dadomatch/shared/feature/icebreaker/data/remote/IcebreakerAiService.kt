package com.dadomatch.shared.feature.icebreaker.data.remote

import com.dadomatch.shared.core.util.Resource

/**
 * Provider-agnostic contract for generating an icebreaker.
 *
 * Implemented by each AI backend (Gemini, NVIDIA NIM, …) and by the
 * [RoutingIcebreakerService] that chains them. The repository depends on this
 * interface, not on any concrete provider.
 */
interface IcebreakerAiService {
    /** Stable id used for telemetry (e.g. "nvidia", "gemini", "routing"). */
    val providerId: String

    suspend fun generateIcebreaker(
        environment: String,
        intensity: String,
        language: String,
        usePremiumModel: Boolean = false,
    ): Resource<String>
}
