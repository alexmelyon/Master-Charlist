package com.github.alexmelyon.master_charlist.room

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.Source

const val FIELD_WORLD_GROUP = "worldGroup"
const val FIELD_NAME = "name"
const val FIELD_ARCHIVED = "archived"
const val FIELD_ORIGIN = "origin"
const val FIELD_USER_UID = "userUid"

open class FirestoreDoc(
    var origin: String = "",
    var deviceId: String = "",
    var userUid: String? = null
) {
    @Exclude
    var firestoreId = ""

    var archived = false
}

class FirestoreService {

    val source = Source.DEFAULT

    fun create() {

    }

    fun modify() {

    }

    fun delete() {

    }
}