package com.helloandroid.dagger

import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import com.helloandroid.BuildConfig
import com.helloandroid.room.AppDatabase
import dagger.Module
import dagger.Provides

@Module
class InmemoryDatabaseModule(val context: Context) {

    val dbName = BuildConfig.ROOM_DB_NAME

    val db: AppDatabase by lazy {
        Log.i("ROOM", "Create ROOM using '$dbName'")
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideDb(): AppDatabase {
        Log.i("ROOM", "Room using '$dbName'")
        return db
    }
}