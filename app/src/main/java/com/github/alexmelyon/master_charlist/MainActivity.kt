package com.github.alexmelyon.master_charlist

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.alexmelyon.master_charlist.list_worlds.ListWorldsController
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.find
import ru.napoleonit.talan.di.HasControllerInjector
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasControllerInjector {

    @Inject
    lateinit var dispatchingControllerInjector: DispatchingAndroidInjector<Controller>

    lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        val content = find<ViewGroup>(android.R.id.content)
        router = Conductor.attachRouter(this, content, savedInstanceState)
        if (!router.hasRootController()) {
            // TODO Save backstack
            router.setRoot(RouterTransaction.with(ListWorldsController()))
        }
        window.decorView.backgroundColor = Color.WHITE
    }

    override fun controllerInjector(): DispatchingAndroidInjector<Controller> {
        return dispatchingControllerInjector
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }
}
