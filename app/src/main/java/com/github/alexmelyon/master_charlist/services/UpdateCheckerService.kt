package com.github.alexmelyon.master_charlist.services

import android.content.Context

class UpdateCheckerService(val context: Context) {

    val getVersionCode = GetVersionCode(context)

    fun checkAvailable(onPost: (Boolean) -> Unit) {
        val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        getVersionCode.onPost = { version ->
            version
        }
        getVersionCode.execute()
    }

    fun openGooglePlay() {

    }

}