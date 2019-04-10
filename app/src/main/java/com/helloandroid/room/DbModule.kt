package com.helloandroid.room

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class DbModule {
    @Provides
    fun provideDb(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "MasterCharlistDB")
            .allowMainThreadQueries()
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE `effect` (" +
                    "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "`name` TEXT NOT NULL," +
                    "`worldGroup` INTEGER NOT NULL," +
                    "`lastUsed` INTEGER NOT NULL," +
                    "`archived` INTEGER NOT NULL)")
            database.execSQL("CREATE TABLE `effectdiff` (" +
                    "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "`value` INTEGER NOT NULL," +
                    "`time` INTEGER NOT NULL," +
                    "`characterGroup` INTEGER NOT NULL," +
                    "`effectGroup` INTEGER NOT NULL," +
                    "`sessionGroup` INTEGER NOT NULL," +
                    "`gameGroup` INTEGER NOT NULL," +
                    "`worldGroup` INTEGER NOT NULL," +
                    "`archived` INTEGER NOT NULL)")
        }
    }
}