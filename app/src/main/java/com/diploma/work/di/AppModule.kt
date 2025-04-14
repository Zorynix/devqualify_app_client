package com.diploma.work.di

import com.diploma.work.data.grpc.AuthGrpcClient
import com.diploma.work.data.grpc.GrpcClient
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.data.repository.AuthRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}