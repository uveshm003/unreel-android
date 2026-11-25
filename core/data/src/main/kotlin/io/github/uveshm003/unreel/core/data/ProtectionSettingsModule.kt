package io.github.uveshm003.unreel.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface ProtectionSettingsModule {
    @Binds fun bindProtectionSettings(repository: RoomProtectionSettingsRepository): ProtectionSettingsRepository
}
