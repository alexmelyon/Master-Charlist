package com.github.alexmelyon.master_charlist.room

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import android.content.Context
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