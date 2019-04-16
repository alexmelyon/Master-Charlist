package com.helloandroid.utils

import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.widget.TextView
import org.jetbrains.anko.singleLine

fun Context.showAlertEditDialog(title: String, message: String = "", action: (String) -> Unit) {
    val edit = TextInputEditText(this)
    edit.singleLine = true
    edit.setText(message, TextView.BufferType.EDITABLE)

    val dialog = AlertDialog.Builder(this)
        .setTitle(title)
        .setView(edit)
        .setPositiveButton("OK", null)
        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        .create()
    dialog.show()
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { v ->
        if (edit.text.isEmpty()) {
            edit.setError("Please enter name")
        } else {
            dialog.dismiss()
            action(edit.text.toString())
        }
    }
    edit.requestFocus()
}

fun Context.showAlertDialog(title: String, message: String, action: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            action()
        })
        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->  })
        .show()
}

fun Context.alertNoAvailableSkills() {
    AlertDialog.Builder(this)
        .setTitle("Master Charlist")
        .setMessage("No available skills")
        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        .show()
}