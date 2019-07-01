package com.helloandroid.dagger

import com.helloandroid.list_games.ListGamesControllerTest
import com.helloandroid.list_worlds.ListWorldsViewTest
import com.helloandroid.list_worlds.ParentTest
import dagger.Component
import dagger.android.AndroidInjectionModule

@Component(modules = [
    MainActivityModule::class,
    AndroidInjectionModule::class,
    InmemoryDatabaseModule::class])
interface TestAppComponent : AppComponent {
    fun inject(activity: ListWorldsViewTest)
    fun inject(activity: ParentTest)
    fun inject(activity: ListGamesControllerTest)
}