package com.angandroid.appmapsdriver.utils_provider

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ConfigProvider {

    val db = Firebase.firestore.collection("Config")

    fun getPrices(): Task<DocumentSnapshot> {
        return db.document("prices").get().addOnFailureListener { exp ->
            Log.d("FIREBASE", "Error -> ${exp.message}")
        }
    }
}