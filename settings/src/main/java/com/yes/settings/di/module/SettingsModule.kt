package com.yes.settings.di.module

import com.yes.settings.data.repository.SettingsRepository
import com.yes.settings.domain.usecase.GetSettingsUseCase
import com.yes.settings.domain.usecase.SetSettingsUseCase
import com.yes.settings.presentation.mapper.MapperUI
import com.yes.settings.presentation.wm.SettingsViewModel
import com.yes.shared.data.dataSource.SettingsDataSource
import com.yes.shared.di.module.IoDispatcher
import com.yes.shared.presentation.vm.BaseDependency
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher

@Module
class SettingsModule {
    @Provides
    fun providesMapperUI(

    ): MapperUI {
        return MapperUI(

        )
    }

    @Provides
    fun providesSetSettingsUseCase(
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): SetSettingsUseCase {
        return SetSettingsUseCase(
            dispatcher,
        )
    }
    @Provides
    fun providesSettingsRepository(
        settingsDataSource: SettingsDataSource
    ): SettingsRepository {
        return SettingsRepository(
            settingsDataSource
        )
    }
    @Provides
    fun providesGetSettingsUseCase(
        @IoDispatcher dispatcher: CoroutineDispatcher,
        settingsRepository: SettingsRepository
    ): GetSettingsUseCase {
        return GetSettingsUseCase(
            dispatcher,
            settingsRepository
        )
    }

    @Provides
    fun providesSettingsViewModelFactory(
        getSettingsUseCase: GetSettingsUseCase,
        setSettingsUseCase: SetSettingsUseCase,
        mapperUI: MapperUI
    ): SettingsViewModel.Factory {
        return SettingsViewModel.Factory(
            getSettingsUseCase,
            setSettingsUseCase,
            mapperUI
        )
    }

    @Provides
    fun providesDependency(
        factory: SettingsViewModel.Factory,
    ): BaseDependency {
        return BaseDependency(
            factory
        )
    }
}