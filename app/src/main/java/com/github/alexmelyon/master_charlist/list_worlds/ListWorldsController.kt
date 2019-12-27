package com.github.alexmelyon.master_charlist.list_worlds

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.crashlytics.android.Crashlytics
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.github.alexmelyon.master_charlist.App
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.room.World
import com.github.alexmelyon.master_charlist.tutorial.TutorialActivity
import com.github.alexmelyon.master_charlist.world_pager.WorldPagerController
import com.google.firebase.auth.FirebaseAuth
import ru.napoleonit.talan.di.ControllerInjector
import java.util.*
import javax.inject.Inject

class ListWorldsController : Controller(), ListWorldsContract.Controller {

    private val RC_SIGN_IN: Int = 1001

    @Inject
    lateinit var view: ListWorldsContract.View

    private val setWorlds: TreeSet<World> = TreeSet(kotlin.Comparator { o1, o2 ->
        val res = o2.createTime.compareTo(o1.createTime)
        if (res == 0) {
            return@Comparator o1.name.compareTo(o2.name)
        }
        return@Comparator res
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        Crashlytics.log(Log.INFO, javaClass.simpleName, "onCreateView")
        setHasOptionsMenu(true)
        return view.createView(container)
    }

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        App.instance.worldStorage.getAll {
            setWorlds.clear()
            setWorlds.addAll(it)
            this.view.setData(setWorlds.toMutableList())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.list_worlds, menu)
        FirebaseAuth.getInstance().currentUser?.let { user ->
            menu.findItem(R.id.menu_login).isVisible = false
            menu.findItem(R.id.menu_username).isVisible = true
            menu.findItem(R.id.menu_username).title = user.displayName
            menu.findItem(R.id.menu_logout).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_world -> {
                view.showCreateWorldDialog()
                return true
            }
            R.id.menu_login -> {
                val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    RC_SIGN_IN)
            }
            R.id.menu_logout -> {
                AuthUI.getInstance()
                    .signOut(activity!!)
                    .addOnCompleteListener {
                        activity?.invalidateOptionsMenu()
                    }
            }
            R.id.menu_show_tutorial -> {
                startActivity(Intent(applicationContext, TutorialActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(TutorialActivity.FORCED_TUTORIAL, true)
                })
                return true
            }
            R.id.menu_about -> {
                view.showAboutDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser!!
                Log.d("JCD", "Login ${user.uid}")
                activity?.invalidateOptionsMenu()

                App.instance.userService.getOrCreate(user.uid, user.displayName ?: "")
                App.instance.worldStorage.updateLocalWorlds {
                    setWorlds.clear()
                    setWorlds.addAll(it)
                    this.view.setData(setWorlds.toMutableList())
                }
            } else {
                AlertDialog.Builder(activity!!)
                    .setTitle(activity?.getString(R.string.error))
                    .setMessage(response?.error?.message)
                    .setPositiveButton("OK", { dialog, which ->  })
                    .show()
            }
        }
    }

    override fun onItemClick(world: World) {
        router.pushController(RouterTransaction.with(WorldPagerController(world.firestoreId)))
    }

    override fun createWorld(worldName: String) {
        val world = App.instance.worldStorage.create(worldName)

        setWorlds.add(world)
        view.addedAt(0, world)
    }

    override fun archiveWorldAt(pos: Int) {
        val world = setWorlds.toList()[pos]
        App.instance.worldStorage.archive(world)

        setWorlds.remove(world)
        view.archivedAt(pos)
    }

    override fun renameWorld(pos: Int, world: World, name: String) {
        App.instance.worldStorage.rename(world, name)
        view.itemChangedAt(pos)
    }
}
