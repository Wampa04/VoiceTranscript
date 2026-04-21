package com.voicetranscript.di

import com.voicetranscript.ndk.WhisperLib
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WhisperModule {

    @Provides
    @Singleton
    fun provideWhisperLib(): WhisperLib {
        return WhisperLib()
    }
}