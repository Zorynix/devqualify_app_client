package com.diploma.work.di

import android.content.Context
import android.content.SharedPreferences
import com.diploma.work.ui.theme.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SearchHistoryPrefs

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppPrefs

@Module
@InstallIn(SingletonComponent::class)
object SharedPreferencesModule {
    @Provides
    @Singleton
    @SearchHistoryPrefs
    fun provideSearchHistoryPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    @AppPrefs
    fun provideAppPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

//    @Provides
//    @Singleton
//    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
//        return context.getSharedPreferences("default_prefs", Context.MODE_PRIVATE)
//    }

    @Provides
    @Singleton
    fun provideThemeManager(@AppPrefs preferences: SharedPreferences): ThemeManager {
        return ThemeManager(preferences)
    }
}
