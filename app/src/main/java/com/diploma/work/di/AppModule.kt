package com.diploma.work.di

import android.content.Context
import com.diploma.work.data.AppSession
import com.diploma.work.data.grpc.AuthGrpcClient
import com.diploma.work.data.grpc.GrpcClient
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.data.repository.AuthRepositoryImpl
import com.diploma.work.ui.theme.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideManagedChannel(): ManagedChannel {
        return ManagedChannelBuilder.forTarget("10.0.2.2:50051")
            .usePlaintext()
            .build()
    }

    @Provides
    @Singleton
    fun provideThemeManager(session: AppSession): ThemeManager {
        return ThemeManager(session)
    }

    @Provides
    @Singleton
    fun provideGrpcClient(): GrpcClient {
        return GrpcClient()
    }

    @Provides
    @Singleton
    fun provideAuthGrpcClient(channel: ManagedChannel): AuthGrpcClient {
        return AuthGrpcClient(channel)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(authGrpcClient: AuthGrpcClient): AuthRepository {
        return AuthRepositoryImpl(authGrpcClient)
    }

    @Provides
    @Singleton
    fun provideAppSession(@ApplicationContext context: Context): AppSession {
        return AppSession(context)
    }
}