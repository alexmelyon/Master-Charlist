package com.helloandroid

import android.support.test.InstrumentationRegistry
import com.helloandroid.dagger.DaggerTestAppComponent
import com.helloandroid.dagger.InmemoryDatabaseModule
import com.helloandroid.dagger.TestAppComponent

open class ParentTest {

    val app: App
    val testAppComponent: TestAppComponent

    init {
        app = InstrumentationRegistry.getTargetContext().applicationContext as App
        val dbModule = InmemoryDatabaseModule(app)
        testAppComponent = DaggerTestAppComponent.builder()
            .inmemoryDatabaseModule(dbModule)
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(app)
    }
}
