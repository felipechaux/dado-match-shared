package com.dadomatch.shared.feature.engagement.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.messaging

/**
 * FCM topics used to target campaigns from the Firebase console:
 * `all`, `lang_en` / `lang_es`, `tier_free` / `tier_pro`.
 */
class PushTopics {

    fun sync(enabled: Boolean, language: String, isPro: Boolean) {
        val wanted = if (enabled) {
            setOf(ALL, if (language.startsWith("es")) LANG_ES else LANG_EN, if (isPro) TIER_PRO else TIER_FREE)
        } else {
            emptySet()
        }
        // Topic calls are idempotent, so every topic is set to its wanted state each time
        // instead of tracking what was subscribed before.
        ALL_TOPICS.forEach { topic ->
            runCatching {
                if (topic in wanted) Firebase.messaging.subscribeToTopic(topic)
                else Firebase.messaging.unsubscribeFromTopic(topic)
            }
        }
    }

    private companion object {
        const val ALL = "all"
        const val LANG_EN = "lang_en"
        const val LANG_ES = "lang_es"
        const val TIER_FREE = "tier_free"
        const val TIER_PRO = "tier_pro"
        val ALL_TOPICS = listOf(ALL, LANG_EN, LANG_ES, TIER_FREE, TIER_PRO)
    }
}
