package com.angandroid.appmapsdriver.utils_provider

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/** Handler data collection Client firestore **/
class ClientProvider {

    // Get collection db
    val db = Firebase.firestore.collection("Clients")

    // Get doc client
    fun getDataClient(idClient: String): Task<DocumentSnapshot> {
        return db.document(idClient).get()
    }
}