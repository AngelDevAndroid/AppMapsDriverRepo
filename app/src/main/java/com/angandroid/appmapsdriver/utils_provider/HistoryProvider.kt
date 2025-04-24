package com.angandroid.appmapsdriver.utils_provider

import android.util.Log
import com.angandroid.appmapsdriver.models.HistoryTripModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Provider
class HistoryProvider {

    private val db = Firebase.firestore.collection("Histories")
    val authProvider = FrbAuthProviders()

    // Asign an id by default to doc
    /*fun create(history: HistoryTripModel): Task<DocumentReference> {
        return db.add(history).addOnFailureListener { error ->
            Log.d("LG_FIRESTORE", "ERROR -> ${error.message}")
        }
    }*/

    // I asign an id custom the doc
    fun createHistTrip(idHist: String, history: HistoryTripModel): Task<Void> {
        return db.document(idHist).set(history).addOnFailureListener { error ->
            Log.d("LG_FIRESTORE", "ERROR -> ${error.message}")
        }
    }

    // Consulta compuesta , requiere collection indexed
    fun getLastHistory(): Query {
        return db.whereEqualTo("idClient", authProvider.getIdFrb())
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .limit(1)
    }

    fun updateRatingClient(idDocHist: String, rating: Float): Task<Void> {
        return db.document(idDocHist)
            .update("ratingToClient", rating).addOnFailureListener { failure ->
                Log.d("LG_FIRESTORE", "ERROR -> ${failure.message}")
            }
    }

    fun getHistoryDriverById(idHistory: String): Task<DocumentSnapshot> {
        return db.document(idHistory).get()
    }

    // Get doc driver
    fun getHistoriesDriver(): Query {
        return db.whereEqualTo("idClient", authProvider.getIdFrb())
            .orderBy("timeStamp", Query.Direction.DESCENDING)
    }

    // Update status trip driver booking
    fun updateStatus(idClient: String, status: String): Task<Void> {
        return db.document(idClient)
                 .update("status", status).addOnFailureListener { failure ->
            Log.d("LG_FIRESTORE", "ERROR -> ${failure.message}")
        }
    }
}