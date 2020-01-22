package com.github.alexmelyon.master_charlist.room

import com.github.alexmelyon.master_charlist.App
import com.google.firebase.firestore.*
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

open class FirestoreCollection<T : FirestoreDoc>(val name: String) {

    val userService by lazy { App.instance.userService }
    val deviceService by lazy { App.instance.deviceService }
    val firestoreService by lazy { App.instance.firestoreService }

    protected val collection by lazy {
        FirebaseFirestore.getInstance().collection(name)
    }

    fun create(item: T, init: T.() -> Unit): Deferred<T> {

        val res = GlobalScope.async {

            val deviceId = deviceService.deviceId
            val userUid = userService.currentUserUid
            val origin = userUid ?: deviceId
            val doc = item.apply {
                this.origin = origin
                this.deviceId = deviceId
                this.userUid = userUid
                this.lastUsed = Calendar.getInstance().time
                init()
            }

            collection.add(doc)
                .addOnSuccessListener {
                    doc.firestoreId = it.id
                }.await()
            return@async doc
        }
        return res
    }

    fun getAll(where: (Query) -> Query, onSuccess: (List<T>) -> Unit) {
        val origins = mutableListOf(deviceService.deviceId)
        userService.currentUserUid?.let { origins.add(it) }
        collection.whereIn(FIELD_ORIGIN, origins)
            .whereEqualTo(FIELD_ARCHIVED, false)
            .apply { where(this) }
            .get(firestoreService.source)
            .addOnSuccessListener { querySnapshot ->
                val docs = querySnapshot.map { docSnapshot ->
                    docSnapshot.toObject(T::class.java).apply {
                        firestoreId = docSnapshot.id
                    }
                }
            }
    }
}