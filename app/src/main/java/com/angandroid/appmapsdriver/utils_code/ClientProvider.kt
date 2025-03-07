package com.angandroid.appmapsdriver.utils_code

import com.angandroid.appmapsdriver.models.ClientModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ClientProvider {
    val db = Firebase.firestore.collection("Clients")

    fun createUser(clientModel: ClientModel): Task<Void>{
        return db.document(clientModel.idUser?: "").set(clientModel)
    }

}