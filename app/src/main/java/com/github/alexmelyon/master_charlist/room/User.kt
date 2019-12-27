package com.github.alexmelyon.master_charlist.room

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class User(var uid: String, var name: String, val createTime: Date)

class UserService {

    val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun getOrCreate(uid: String, name: String): User {
        val user = User(uid, name, Calendar.getInstance().time)
        FirebaseFirestore.getInstance().collection("users").document(uid).set(user)
        return user
    }
}