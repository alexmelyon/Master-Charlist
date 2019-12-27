package com.github.alexmelyon.master_charlist.room

import com.google.firebase.iid.FirebaseInstanceId

class DeviceService {
    val deviceId by lazy {
        FirebaseInstanceId.getInstance().id
    }
}