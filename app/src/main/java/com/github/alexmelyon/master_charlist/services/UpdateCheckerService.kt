package com.github.alexmelyon.master_charlist.services

import android.content.Context
import javax.inject.Inject

class UpdateCheckerService {

    @Inject
    lateinit var context: Context

    fun isUpdateAvailable(): Boolean {
        context.packageManager.packagen
    }

    fun openGooglePlay() {

    }
}