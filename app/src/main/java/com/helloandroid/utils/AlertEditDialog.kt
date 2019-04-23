package com.helloandroid.utils

import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.helloandroid.MainActivity
import org.jetbrains.anko.singleLine
import android.view.WindowManager
import com.helloandroid.R


fun MainActivity.showAlertEditDialog(title: String, message: String = "", action: (String) -> Unit) {
    val edit = TextInputEditText(this)
    edit.id = R.id.alert_edit
    edit.singleLine = true
    edit.inputType = EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
    edit.setText(message, TextView.BufferType.EDITABLE)

    val dialog = AlertDialog.Builder(this)
        .setTitle(title)
        .setView(edit)
        .setPositiveButton("OK", null)
        .setNegativeButton("Cancel", null)
        .create()
    dialog.show()

    edit.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { v ->
        if (edit.text.isBlank()) {
            edit.error = "Please enter name"
        } else {
            dialog.dismiss()
            action(edit.text.toString())
        }
    }
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