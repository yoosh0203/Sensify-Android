package com.seung.sensormaster.di

import android.content.Context
import com.seung.sensormaster.data.sensor.SensorDataManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSensorDataManager(
        @ApplicationContext context: Context
    ): SensorDataManager = SensorDataManager(context)
}
