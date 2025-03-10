package com.angandroid.appmapsdriver.utils_code

import com.angandroid.appmapsdriver.models.ClientModel
import com.angandroid.appmapsdriver.models.DriverModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DriverProvider {
    val db = Firebase.firestore.collection("Drivers")

    fun createUser(driverModel: DriverModel): Task<Void>{
        return db.document(driverModel.idUser?: "").set(driverModel)
    }

}