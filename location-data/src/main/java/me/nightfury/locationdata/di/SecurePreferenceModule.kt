package me.nightfury.locationdata.di

import android.content.Context
import com.google.crypto.tink.Aead
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.nightfury.locationdata.security.SecureDataStore
import me.nightfury.locationdata.security.SecureKeyManager
import me.nightfury.locationdomain.repo.SecureStorage
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object SecurePreferenceModule {

    @Provides
    @Singleton
    fun provideAead(@ApplicationContext context: Context): Aead =
        SecureKeyManager.getAead(context)

    @Provides
    @Singleton
    fun provideSecureDataStore(
        @ApplicationContext context: Context,
        aead: Aead
    ): SecureStorage = SecureDataStore(context, aead)
}