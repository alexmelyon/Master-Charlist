package com.github.alexmelyon.master_charlist.list_effects

import com.bluelinelabs.conductor.Controller
import com.github.alexmelyon.master_charlist.dagger.ControllerKey
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap

@Module(subcomponents = [ListEffectsSubcomponent::class])
interface ListEffectsControllerModule {

    @Binds
    @IntoMap
    @ControllerKey(ListEffectsController::class)
    fun provideInjectorFactory(builder: ListEffectsSubcomponent.Builder): AndroidInjector.Factory<out Controller>

    @Binds
    fun provideView(view: ListEffectsView): ListEffectsContract.View

    @Binds
    fun provideController(controller: ListEffectsController): ListEffectsContract.Controller
}

@Subcomponent
interface ListEffectsSubcomponent : AndroidInjector<ListEffectsController> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ListEffectsController>()
}