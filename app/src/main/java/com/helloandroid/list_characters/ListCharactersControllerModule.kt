package com.helloandroid.list_characters

import com.bluelinelabs.conductor.Controller
import com.helloandroid.dagger.ControllerKey
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap

@Module(subcomponents = [ListCharactersSubmodule::class])
interface ListCharactersControllerModule {
    @Binds
    @IntoMap
    @ControllerKey(ListCharactersController::class)
    fun provideInjectorFactory(builder: ListCharactersSubmodule.Builder) : AndroidInjector.Factory<out Controller>

    @Binds
    fun bindView(view: ListCharactersView): ListCharactersContract.View

    @Binds
    fun bindController(controller: ListCharactersController): ListCharactersContract.Controller
}

@Subcomponent
interface ListCharactersSubmodule : AndroidInjector<ListCharactersController> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ListCharactersController>()
}