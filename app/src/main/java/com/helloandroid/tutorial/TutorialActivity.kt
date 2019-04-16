package com.helloandroid.tutorial

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.helloandroid.MainActivity
import com.helloandroid.R
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity


class TutorialActivity : TutorialActivity() {

    private val SHARED = "SHARED"
    private val TUTORIAL = "TUTORIAL"

    companion object {
        const val FORCED_TUTORIAL = "FORCED_TUTORIAL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val forcedTutorial = intent.extras?.getBoolean(FORCED_TUTORIAL) ?: false

        if (!forcedTutorial && getSharedPreferences(SHARED, Context.MODE_PRIVATE).getBoolean(TUTORIAL, false)) {
            startMainActivity()
        }

        addFragment(
            Step.Builder().setTitle("Create worlds")
                .setBackgroundColor(Color.parseColor("#ef5350"))
                .setDrawable(R.drawable.t1)
                .build()
        )
        addFragment(
            Step.Builder().setTitle("Create games")
                .setBackgroundColor(Color.parseColor("#ec407a"))
                .setDrawable(R.drawable.t2)
                .build()
        )
        addFragment(
            Step.Builder().setTitle("Create characters")
                .setBackgroundColor(Color.parseColor("#ab47bc"))
                .setDrawable(R.drawable.t3)
                .build()
        )
        addFragment(
            Step.Builder().setTitle("Create new session")
                .setBackgroundColor(Color.parseColor("#7e57c2"))
                .setDrawable(R.drawable.t4)
                .build()
        )
        addFragment(
            Step.Builder().setTitle("Change character abilities")
                .setBackgroundColor(Color.parseColor("#5c6bc0"))
                .setDrawable(R.drawable.t5)
                .build()
        )
        addFragment(
            Step.Builder().setTitle("Close the session")
                .setBackgroundColor(Color.parseColor("#42a5f5"))
                .setDrawable(R.drawable.t6)
                .build()
        )
        addFragment(
            Step.Builder().setTitle("And see results")
                .setBackgroundColor(Color.parseColor("#42a5f5"))
                .setDrawable(R.drawable.t7)
                .build()
        )
    }

    override fun currentFragmentPosition(pos: Int) {
        Log.d("JCD", "POSITION $pos")
    }

    override fun finishTutorial() {
        super.finishTutorial()
        getSharedPreferences(SHARED, Context.MODE_PRIVATE).edit().apply {
            putBoolean(TUTORIAL, true)
            apply()
        }
        startMainActivity()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}