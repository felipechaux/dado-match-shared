package com.dadomatch.shared.di

/**
 * Returns a list of all Koin modules in the application,
 * structured by feature responsibility.
 */
fun getAllModules() = listOf(
    coreModule,
    icebreakerModule,
    successModule,
    subscriptionModule
)
