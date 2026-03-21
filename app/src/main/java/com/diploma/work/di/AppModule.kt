package com.diploma.work.di

import android.content.Context
import com.diploma.work.data.AppSession
import com.diploma.work.data.api.ArticlesApi
import com.diploma.work.data.api.AuthApi
import com.diploma.work.data.api.AuthInterceptor
import com.diploma.work.data.api.InstantTypeAdapter
import com.diploma.work.data.api.IntTypeAdapter
import com.diploma.work.data.api.LongTypeAdapter
import com.diploma.work.data.api.TestsApi
import com.diploma.work.data.api.UserInfoApi
import com.diploma.work.data.cache.ArticlesCacheManager
import com.diploma.work.data.preferences.UserPreferencesManager
import com.diploma.work.data.repository.ArticlesRepository
import com.diploma.work.data.repository.ArticlesRepositoryImpl
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.data.repository.AuthRepositoryImpl
import com.diploma.work.data.repository.TestsRepository
import com.diploma.work.data.repository.TestsRepositoryImpl
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.data.repository.UserInfoRepositoryImpl
import com.diploma.work.data.security.AvatarManager
import com.diploma.work.data.security.SecureTokenManager
import com.diploma.work.ui.theme.ThemeManager
import com.diploma.work.utils.Constants
import com.diploma.work.utils.ErrorHandler
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Long::class.java, LongTypeAdapter())
        .registerTypeAdapter(Long::class.javaPrimitiveType, LongTypeAdapter())
        .registerTypeAdapter(Int::class.java, IntTypeAdapter())
        .registerTypeAdapter(Int::class.javaPrimitiveType, IntTypeAdapter())
        .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
        .create()

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(Constants.Network.CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.Network.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.Network.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.Network.GATEWAY_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUserInfoApi(retrofit: Retrofit): UserInfoApi = retrofit.create(UserInfoApi::class.java)

    @Provides
    @Singleton
    fun provideTestsApi(retrofit: Retrofit): TestsApi = retrofit.create(TestsApi::class.java)

    @Provides
    @Singleton
    fun provideArticlesApi(retrofit: Retrofit): ArticlesApi = retrofit.create(ArticlesApi::class.java)

    @Provides
    @Singleton
    fun provideSecureTokenManager(@ApplicationContext context: Context): SecureTokenManager {
        return SecureTokenManager(context)
    }

    @Provides
    @Singleton
    fun provideAvatarManager(@ApplicationContext context: Context): AvatarManager {
        return AvatarManager(context)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesManager(@ApplicationContext context: Context): UserPreferencesManager {
        return UserPreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideArticlesCacheManager(@ApplicationContext context: Context): ArticlesCacheManager {
        return ArticlesCacheManager(context)
    }

    @Provides
    @Singleton
    fun provideThemeManager(session: AppSession): ThemeManager {
        return ThemeManager(session)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository {
        return AuthRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideUserInfoRepository(api: UserInfoApi): UserInfoRepository {
        return UserInfoRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideTestsRepository(api: TestsApi, @ApplicationContext context: Context): TestsRepository {
        return TestsRepositoryImpl(api, context)
    }

    @Provides
    @Singleton
    fun provideArticlesRepository(api: ArticlesApi): ArticlesRepository {
        return ArticlesRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideAppSession(
        @ApplicationContext context: Context,
        secureTokenManager: SecureTokenManager,
        avatarManager: AvatarManager,
        userPreferencesManager: UserPreferencesManager,
        articlesCacheManager: ArticlesCacheManager
    ): AppSession {
        return AppSession(context, secureTokenManager, avatarManager, userPreferencesManager, articlesCacheManager)
    }

    @Provides
    @Singleton
    fun provideErrorHandler(@ApplicationContext context: Context): ErrorHandler {
        return ErrorHandler(context)
    }
}
