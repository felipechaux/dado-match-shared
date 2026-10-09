package com.dadomatch.shared.feature.game.di

import com.dadomatch.shared.feature.game.data.local.GameLocalDataSource
import com.dadomatch.shared.feature.game.data.repository.GameRepositoryImpl
import com.dadomatch.shared.feature.game.domain.repository.GameRepository
import com.dadomatch.shared.feature.game.domain.usecase.FetchChallengesUseCase
import com.dadomatch.shared.feature.game.domain.usecase.StartGameUseCase
import com.dadomatch.shared.feature.game.presentation.GameViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Feature: Game mode (Fiesta / Cita). Challenges come from the same AI router as
 * the icebreakers (icebreakerModule).
 */
val gameModule = module {
    singleOf(::GameLocalDataSource)
    singleOf(::GameRepositoryImpl) bind GameRepository::class
    factory { StartGameUseCase(get(), get()) }
    factoryOf(::FetchChallengesUseCase)
    viewModel { GameViewModel(get(), get(), get(), get(), get(), get()) }
}
