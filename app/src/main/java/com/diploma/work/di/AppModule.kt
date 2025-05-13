package com.diploma.work.di

import android.content.Context
import com.diploma.work.data.AppSession
import com.diploma.work.data.grpc.AuthGrpcClient
import com.diploma.work.data.grpc.GrpcClient
import com.diploma.work.data.grpc.TestsGrpcClient
import com.diploma.work.data.grpc.UserInfoGrpcClient
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.data.repository.AuthRepositoryImpl
import com.diploma.work.data.repository.TestsRepository
import com.diploma.work.data.repository.TestsRepositoryImpl
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.data.repository.UserInfoRepositoryImpl
import com.diploma.work.ui.theme.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthChannel

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserInfoChannel

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TestsChannel

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @AuthChannel
    @Provides
    @Singleton
    fun provideAuthManagedChannel(): ManagedChannel {
        return ManagedChannelBuilder.forTarget("10.0.2.2:50051")
            .usePlaintext()
            .build()
    }
    
    @UserInfoChannel
    @Provides
    @Singleton
    fun provideUserInfoManagedChannel(): ManagedChannel {
        return ManagedChannelBuilder.forTarget("10.0.2.2:50052")
            .usePlaintext()
            .build()
    }

    @TestsChannel
    @Provides
    @Singleton
    fun provideTestsManagedChannel(): ManagedChannel {
        return ManagedChannelBuilder.forTarget("10.0.2.2:50053")
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
    fun provideAuthGrpcClient(@AuthChannel channel: ManagedChannel): AuthGrpcClient {
        return AuthGrpcClient(channel)
    }

    @Provides
    @Singleton
    fun provideUserInfoGrpcClient(@UserInfoChannel channel: ManagedChannel): UserInfoGrpcClient {
        return UserInfoGrpcClient(channel)
    }

    @Provides
    @Singleton
    fun provideTestsGrpcClient(@TestsChannel channel: ManagedChannel, session: AppSession): TestsGrpcClient {
        return TestsGrpcClient(channel, session)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(authGrpcClient: AuthGrpcClient): AuthRepository {
        return AuthRepositoryImpl(authGrpcClient)
    }

    @Provides
    @Singleton
    fun provideUserInfoRepository(userInfoGrpcClient: UserInfoGrpcClient): UserInfoRepository {
        return UserInfoRepositoryImpl(userInfoGrpcClient)
    }

    @Provides
    @Singleton
    fun provideTestsRepository(testsGrpcClient: TestsGrpcClient, @ApplicationContext context: Context): TestsRepository {
        return TestsRepositoryImpl(testsGrpcClient, context)
    }

    @Provides
    @Singleton
    fun provideAppSession(@ApplicationContext context: Context): AppSession {
        return AppSession(context)
    }
}