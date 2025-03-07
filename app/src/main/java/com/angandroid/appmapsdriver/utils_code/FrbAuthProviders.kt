package com.angandroid.appmapsdriver.utils_code

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class FrbAuthProviders() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registerUser(email: String, password: String): Task<AuthResult>{
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun loginUser(email: String, password: String): Task<AuthResult>{
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun getIdFrb(): String {
        return auth.currentUser?.uid ?: ""
    }
}