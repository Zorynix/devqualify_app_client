package com.diploma.work.di

import android.content.Context
import com.diploma.work.data.AppSession
import com.diploma.work.data.grpc.ArticlesGrpcClient
import com.diploma.work.data.grpc.AuthGrpcClient
import com.diploma.work.data.grpc.GrpcClient
import com.diploma.work.data.grpc.TestsGrpcClient
import com.diploma.work.data.grpc.UserInfoGrpcClient
import com.diploma.work.data.repository.ArticlesRepository
import com.diploma.work.data.repository.ArticlesRepositoryImpl
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.data.repository.AuthRepositoryImpl
import com.diploma.work.data.repository.TestsRepository
import com.diploma.work.data.repository.TestsRepositoryImpl
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.data.repository.UserInfoRepositoryImpl
import com.diploma.work.ui.theme.ThemeManager
import com.diploma.work.utils.Constants
import com.diploma.work.utils.ErrorHandler
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

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArticlesChannel

@Module
@InstallIn(SingletonComponent::class)
object AppModule {    @AuthChannel
    @Provides
    @Singleton
    fun provideAuthManagedChannel(): ManagedChannel {
        return ManagedChannelBuilder.forTarget("${Constants.Network.AUTH_SERVER_HOST}:${Constants.Network.AUTH_SERVER_PORT}")
            .usePlaintext()
            .keepAliveTime(Constants.Network.CONNECTION_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .keepAliveTimeout(Constants.Network.READ_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .build()
    }
    
    @UserInfoChannel
    @Provides
    @Singleton
    fun provideUserInfoManagedChannel(): ManagedChannel {
        return ManagedChannelBuilder.forTarget("${Constants.Network.USER_INFO_SERVER_HOST}:${Constants.Network.USER_INFO_SERVER_PORT}")
            .usePlaintext()
            .keepAliveTime(Constants.Network.CONNECTION_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .keepAliveTimeout(Constants.Network.READ_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .build()
    }    @TestsChannel
    @Provides
    @Singleton
    fun provideTestsManagedChannel(): ManagedChannel {
        return ManagedChannelBuilder.forTarget("${Constants.Network.TESTS_SERVER_HOST}:${Constants.Network.TESTS_SERVER_PORT}")
            .usePlaintext()
            .keepAliveTime(Constants.Network.CONNECTION_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .keepAliveTimeout(Constants.Network.READ_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .build()
    }

    @ArticlesChannel
    @Provides
    @Singleton
    fun provideArticlesManagedChannel(): ManagedChannel {
        return ManagedChannelBuilder.forTarget("${Constants.Network.ARTICLES_SERVER_HOST}:${Constants.Network.ARTICLES_SERVER_PORT}")
            .usePlaintext()
            .keepAliveTime(Constants.Network.CONNECTION_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .keepAliveTimeout(Constants.Network.READ_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
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
    fun provideUserInfoGrpcClient(@UserInfoChannel channel: ManagedChannel, session: AppSession): UserInfoGrpcClient {
        return UserInfoGrpcClient(channel, session)
    }

    @Provides
    @Singleton
    fun provideTestsGrpcClient(@TestsChannel channel: ManagedChannel, session: AppSession, @ApplicationContext context: Context): TestsGrpcClient {
        return TestsGrpcClient(channel, session, context)
    }

    @Provides
    @Singleton
    fun provideArticlesGrpcClient(@ArticlesChannel channel: ManagedChannel, session: AppSession): ArticlesGrpcClient {
        return ArticlesGrpcClient(channel, session)
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
    fun provideArticlesRepository(articlesGrpcClient: ArticlesGrpcClient): ArticlesRepository {
        return ArticlesRepositoryImpl(articlesGrpcClient)
    }

    @Provides
    @Singleton
    fun provideAppSession(@ApplicationContext context: Context): AppSession {
        return AppSession(context)
    }

    @Provides
    @Singleton
    fun provideErrorHandler(@ApplicationContext context: Context): ErrorHandler {
        return ErrorHandler(context)
    }
}