package io.github.uveshm003.unreel.core.common.time

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

/** The only source of time in the app. Tests use `TestClock` (from `:core:testing`). */
@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    fun providesClock(): Clock = SystemZoneClock
}
