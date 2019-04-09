package com.helloandroid.utils

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.widget.EditText

fun Context.showAlertEditDialog(title: String, action: (String) -> Unit) {
    val editText = EditText(this)
    AlertDialog.Builder(this)
        .setTitle("Thing name:")
        .setView(editText)
        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            action.invoke(editText.text.toString())
        })
        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        .show()
    editText.requestFocus()
}