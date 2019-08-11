package com.github.alexmelyon.master_charlist.list_worlds

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.room.AppDatabase
import com.github.alexmelyon.master_charlist.room.World
import com.github.alexmelyon.master_charlist.tutorial.TutorialActivity
import com.github.alexmelyon.master_charlist.world_pager.WorldPagerController
import com.google.firebase.auth.FirebaseAuth
import ru.napoleonit.talan.di.ControllerInjector
import java.util.*
import javax.inject.Inject

class ListWorldsController : Controller(), ListWorldsContract.Controller {

    private val REQ_CODE_SIGN_IN = 100

    @Inject
    lateinit var view: ListWorldsContract.View
    @Inject
    lateinit var db: AppDatabase

    private lateinit var setWorlds: TreeSet<World>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        setHasOptionsMenu(true)
        return view.createView(container)
    }

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        setWorlds = TreeSet(kotlin.Comparator { o1, o2 ->
            val res = o2.createTime.compareTo(o1.createTime)
            if (res == 0) {
                return@Comparator o1.name.compareTo(o2.name)
            }
            return@Comparator res
        })
        setWorlds.addAll(db.worldDao().getAll(archived = false))
        this.view.setData(setWorlds.toMutableList())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.list_worlds, menu)

        val login = menu.findItem(R.id.menu_login)
        val logout = menu.findItem(R.id.menu_logout)
        if(FirebaseAuth.getInstance().currentUser != null) {
            login.isVisible = false
        } else {
            logout.isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_world -> {
                view.showCreateWorldDialog()
                return true
            }
            R.id.menu_show_tutorial -> {
                startActivity(Intent(applicationContext, TutorialActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(TutorialActivity.FORCED_TUTORIAL, true)
                })
                return true
            }
            R.id.menu_login -> {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.PhoneBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.FacebookBuilder().build())
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    REQ_CODE_SIGN_IN)
            }
            R.id.menu_logout -> {
                AuthUI.getInstance()
                    .signOut(this.activity!!)
                    .addOnCompleteListener {
                        Log.i("JCD", "LOGOUT")
                    }
            }
            R.id.menu_about -> {
                view.showAboutDialog()
                return true
            }
            // TODO Check new version
            // TODO Check authorized
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CODE_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                Log.i("JCD", "LOGGED " + user?.uid)
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    override fun onItemClick(world: World) {
        router.pushController(RouterTransaction.with(WorldPagerController(world.id)))
    }

    override fun createWorld(worldName: String) {
        val world = World(worldName, Calendar.getInstance().time)
        val id = db.worldDao().insert(world)
        world.id = id

        setWorlds.add(world)
        view.addedAt(0, world)
    }

    override fun archiveWorldAt(pos: Int) {
        val world = setWorlds.toList()[pos]
        world.archived = true
        db.worldDao().update(world)

        setWorlds.remove(world)
        view.archivedAt(pos)
    }

    override fun renameWorld(pos: Int, world: World, name: String) {
        world.name = name
        db.worldDao().update(world)
        view.itemChangedAt(pos)
    }
}
