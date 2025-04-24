package com.angandroid.appmapsdriver.utils_provider

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore

class GeoProvider {

    // Delete collections in firebase
    val delCollection = FirebaseFirestore.getInstance().collection("Locations")
    val delCollecWorking = FirebaseFirestore.getInstance().collection("LocationsWorking")


    val delGeoFirestore = GeoFirestore(delCollection)

    val geoFirestore = GeoFirestore(FirebaseFirestore.getInstance().collection("Locations"))
    val geoFaiCollectionWorking = GeoFirestore(delCollecWorking)

    fun saveLocation(idDriver: String, position: LatLng) {
        geoFirestore.setLocation(idDriver, GeoPoint(position.latitude, position.longitude))
    }

    fun saveLocationWorking(idDriver: String, position: LatLng) {
        geoFaiCollectionWorking.setLocation(idDriver, GeoPoint(position.latitude, position.longitude))
    }

    fun removeLocationOnly(idDriver: String) {
        geoFirestore.removeLocation(idDriver)
    }

    fun delCollLocationAllTree(idDriver: String) {
        delCollection.document(idDriver).delete()
    }

    fun delCollWorking(idDriver: String) {
        delCollecWorking.document(idDriver).delete()
    }

    fun getLocIsConnected(idDriver: String): Task<DocumentSnapshot> {
        return delCollection.document(idDriver).get().addOnFailureListener { exp ->
            Log.d("DEB_EXP", "ERROR-> ${exp.message.toString()}")
        }
    }
}