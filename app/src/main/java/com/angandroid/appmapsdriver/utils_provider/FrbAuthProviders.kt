package com.angandroid.appmapsdriver.utils_provider

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

// Auth user, handler db users(drivers)
class FrbAuthProviders() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Add new user (Driver)
    fun registerUser(email: String, password: String): Task<AuthResult>{
        return auth.createUserWithEmailAndPassword(email, password)
    }

    // Login
    fun loginUser(email: String, password: String): Task<AuthResult>{
        return auth.signInWithEmailAndPassword(email, password)
    }

    // ID to the driver logged
    fun getIdFrb(): String {
        return auth.currentUser?.uid ?: ""
    }

    // Log out auth firebase
    fun logOut() {
        auth.signOut()
    }
}