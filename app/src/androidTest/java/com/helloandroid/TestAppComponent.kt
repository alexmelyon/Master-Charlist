package com.helloandroid

import com.helloandroid.dagger.AppComponent
import com.helloandroid.dagger.MainActivityModule
import com.helloandroid.room.AppDatabaseModule
import dagger.Component
import dagger.android.AndroidInjectionModule

@Component(modules = [
    MainActivityModule::class,
    AndroidInjectionModule::class,
    AppDatabaseModule::class])
interface TestAppComponent : AppComponent {
    fun inject(activity: MainActivityTest)
}