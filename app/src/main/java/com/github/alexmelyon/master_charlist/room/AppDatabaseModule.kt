package com.github.alexmelyon.master_charlist.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides

@Module
class AppDatabaseModule(val context: Context) {
    @Provides
    fun provideDb(): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "MasterCharlistDB")
            .allowMainThreadQueries()
            .build()
    }
}