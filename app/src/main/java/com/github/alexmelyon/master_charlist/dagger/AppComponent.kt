package com.github.alexmelyon.master_charlist.dagger

import android.app.Activity
import android.content.Context
import com.github.alexmelyon.master_charlist.App
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.game_pager.GamePagerControllerModule
import com.github.alexmelyon.master_charlist.list_characters.ListCharactersControllerModule
import com.github.alexmelyon.master_charlist.list_effects.ListEffectsControllerModule
import com.github.alexmelyon.master_charlist.list_games.ListGamesControllerModule
import com.github.alexmelyon.master_charlist.list_sessions.ListSessionsControllerModule
import com.github.alexmelyon.master_charlist.list_skills.ListSkillsControllerModule
import com.github.alexmelyon.master_charlist.list_things.ListThingsControllerModule
import com.github.alexmelyon.master_charlist.list_worlds.ListWorldsControllerModule
import com.github.alexmelyon.master_charlist.room.AppDatabaseModule
import com.github.alexmelyon.master_charlist.session.SessionControllerModule
import com.github.alexmelyon.master_charlist.world_pager.WorldPagerControllerModule
import dagger.*
import dagger.android.ActivityKey
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Singleton
@Subcomponent(modules = [
    ListWorldsControllerModule::class,
    ListGamesControllerModule::class,
    ListSkillsControllerModule::class,
    ListThingsControllerModule::class,
    ListEffectsControllerModule::class,
    ListCharactersControllerModule::class,
    ListSessionsControllerModule::class,
    SessionControllerModule::class,
    WorldPagerControllerModule::class,
    GamePagerControllerModule::class
])
interface MainActivitySubcomponent : AndroidInjector<MainActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MainActivity>()
}

@Module(subcomponents = [MainActivitySubcomponent::class])
abstract class MainActivityModule(val context: Context) {
    @Binds
    @IntoMap
    @ActivityKey(MainActivity::class)
    abstract fun bindMainActivityInjectorFactory(builder: MainActivitySubcomponent.Builder): AndroidInjector.Factory<out Activity>
}

@Component(modules = [
    MainActivityModule::class,
    AndroidInjectionModule::class,
    AppDatabaseModule::class])
interface AppComponent {
    fun inject(app: App)
}