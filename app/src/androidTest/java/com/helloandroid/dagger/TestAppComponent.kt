package com.helloandroid.dagger

import com.helloandroid.list_games.ListGamesControllerTest
import com.helloandroid.list_worlds.ListWorldsControllerTest
import dagger.Component
import dagger.android.AndroidInjectionModule

@Component(
    modules = [
        MainActivityModule::class,
        AndroidInjectionModule::class,
        InmemoryDatabaseModule::class]
)
interface TestAppComponent : AppComponent {
    fun inject(activity: ListWorldsControllerTest)
    fun inject(activity: ListGamesControllerTest)
}