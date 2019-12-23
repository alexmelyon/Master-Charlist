package com.github.alexmelyon.master_charlist.utils

import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.R
import com.google.android.material.textfield.TextInputEditText
import org.jetbrains.anko.singleLine

fun MainActivity.showAlertEditDialog(title: String, message: String = "", okAction: (String) -> Unit) {
    val edit = TextInputEditText(this)
    edit.singleLine = true
    edit.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
    edit.setText(message, TextView.BufferType.EDITABLE)

    val dialog = AlertDialog.Builder(this)
        .setTitle(title)
        .setView(edit)
        .setPositiveButton("OK", null)
        .setNegativeButton(getString(R.string.cancel), null)
        .create()
    dialog.show()

    edit.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { v ->
        if (edit.text?.isBlank() ?: false) {
            edit.error = getString(R.string.please_enter_name)
        } else {
            dialog.dismiss()
            okAction(edit.text?.trim().toString())
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
        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> })
        .show()
}