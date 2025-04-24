package com.angandroid.appmapsdriver.utils_provider

import android.net.Uri
import android.util.Log
import com.angandroid.appmapsdriver.models.DriverModel
import com.angandroid.appmapsdriver.models.HistoryTripModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File

// Data provider driver
class DriverProvider {

    // Get collection db
    val db = Firebase.firestore.collection("Drivers")
    var storageRef = FirebaseStorage.getInstance().getReference().child("ImgProfile")

    // Save user driver
    fun createUser(driverModel: DriverModel): Task<Void>{
        return db.document(driverModel.idUser?: "").set(driverModel)
    }

    // Get doc driver
    fun getDataDriver(idDriver: String): Task<DocumentSnapshot> {
        return db.document(idDriver).get()
    }

    // Update data driver
    fun updateInfoDriver(driver: DriverModel): Task<Void> {

        val map: MutableMap<String, Any> = HashMap()
            map["nameUser"] = driver.nameUser?: ""
            map["imgUser"] = driver.imgUser?: ""
            map["plateNumber"] = driver.plateNumber?: ""
            map["colorCar"] = driver.colorCar?: ""
            map["brandCar"] = driver.brandCar?: ""

        return db.document(driver.idUser.toString())
            .update(map).addOnFailureListener { failure ->
                Log.d("LG_FIRESTORE", "ERROR -> ${failure.message}")
            }
    }

    fun uploadFileStorage(idDriver: String, filePath: File): StorageTask<UploadTask.TaskSnapshot> {

        val file = Uri.fromFile(filePath)

        val riversRef = storageRef.child("${idDriver}.jpg")
        storageRef = riversRef // Reacigne to access to child img
        val uploadTask = riversRef.putFile(file)

        return uploadTask.addOnFailureListener { failure ->
            Log.d("LG_STORAGE", "ERROR -> ${failure.message}")
        }
    }

    fun getImageUrlStorage(): Task<Uri> {
        return storageRef.downloadUrl
            .addOnFailureListener { error ->
                Log.e("LG_STORAGE", "Error al obtener la URL", error)
            }
    }
}

/*
Librery to select image or take photo
Dhaval2404
ImagePicker*/
