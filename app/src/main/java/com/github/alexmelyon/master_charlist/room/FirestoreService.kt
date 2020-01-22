package com.github.alexmelyon.master_charlist.room

import com.github.alexmelyon.master_charlist.App
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

const val WORLD_GROUP = "worldGroup"
const val FIELD_NAME = "name"
const val FIELD_ARCHIVED = "archived"
const val FIELD_ORIGIN = "origin"
const val FIELD_USER_UID = "userUid"

open class FirestoreDoc(var origin: String = "", var deviceId: String = "", var userUid: String? = null) {

    var lastUsed = Calendar.getInstance().time
    var archived = false

    @Exclude
    var firestoreId: String = ""
}

class FirestoreService {

    val source = Source.DEFAULT
}

open class FirestoreCollection<T>(val name: String) {

    val userService by lazy { App.instance.userService }
    val deviceService by lazy { App.instance.deviceService }

    protected val collection by lazy {
        FirebaseFirestore.getInstance().collection(name)
    }

    fun create(init: T.() -> Unit): Deferred<T> {

        val res = GlobalScope.async {

            val deviceId = deviceService.deviceId
            val userUid = userService.currentUserUid
            val origin = userUid ?: deviceId
            val doc = FirestoreDoc(origin, deviceId, userUid).apply {
                init(this as T)
            }

            collection.add(doc)
                .addOnSuccessListener {
                    doc.firestoreId = it.id
                }.await()
            return@async doc as T
        }
        return res
    }
}