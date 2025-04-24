package com.angandroid.appmapsdriver.utils_provider

import android.util.Log
import com.angandroid.appmapsdriver.models.Booking
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Provider
class BookingProvider {

    val db = Firebase.firestore.collection("Bookings")
    val authProvider = FrbAuthProviders()

    fun create(booking: Booking): Task<Void> {
        return db.document(authProvider.getIdFrb()).set(booking).addOnFailureListener { error ->
            Log.d("LG_FIRESTORE", "ERROR -> ${error.message}")
        }
    }

    fun getBooking(): Query {
        return db.whereEqualTo("idDriver", authProvider.getIdFrb())
    }

    // Update status trip driver booking
    fun updateStatus(idClient: String, status: String): Task<Void> {
        return db.document(idClient).update("status", status).addOnFailureListener { failure ->
            Log.d("LG_FIRESTORE", "ERROR -> ${failure.message}")
        }
    }


}