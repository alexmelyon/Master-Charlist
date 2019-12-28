package com.github.alexmelyon.master_charlist

import android.app.Activity
import androidx.multidex.MultiDexApplication
import com.github.alexmelyon.master_charlist.dagger.AppComponent
import com.github.alexmelyon.master_charlist.dagger.DaggerAppComponent
import com.github.alexmelyon.master_charlist.room.*
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject


class App : MultiDexApplication(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingAndroidInjector
    }

    companion object {
        lateinit var instance: App
            private set
        lateinit var appComponent: AppComponent
            private set
    }

    val userService = UserService()
    val deviceService = DeviceService()
    val worldStorage = WorldStorage(userService, deviceService)
    val gameStorage = GameStorage(userService, deviceService)

    override fun onCreate() {
        super.onCreate()
        instance = this

        appComponent = DaggerAppComponent.builder()
            .appDatabaseModule(AppDatabaseModule(this))
            .build()
        appComponent.inject(this)

    }
}
// TODO Комментарий по персонажу, Состояния, особенности (плюсы минусы), дополнительные скиллы, заклинания, баффы, дебаффы, ачивки
// TODO Пресеты по DND, Fallout, SW:KotOR, VTM
// TODO Кубик