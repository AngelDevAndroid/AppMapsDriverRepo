package com.angandroid.appmapsdriver.data_local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data_loc")

class DataStoreManager(private val context: Context) {

    companion object {

        val SAVE_TOKEN_FCM = stringPreferencesKey("token_fcm")

        //val USERNAME_KEY = stringPreferencesKey("username")
        //val PASSWORD_KEY = stringPreferencesKey("password")
        //private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        //val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }

    suspend fun saveTokenFcm(token: String) {
        context.dataStore.edit { preferences ->
            preferences[SAVE_TOKEN_FCM] = token
        }
    }

    suspend fun getTokenFcm(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[SAVE_TOKEN_FCM]
    }

/*// Save data
suspend fun saveUserCred(userName: String, userPassw: String, phone: String) {
    context.dataStore.edit { preferences ->
        preferences[USERNAME_KEY] = userName
        preferences[PASSWORD_KEY] = userPassw
        preferences[PHONE_KEY] = phone

        preferences[IS_LOGGED_IN_KEY] = true
    }
}

/*// Check if user is logged in
val LoggedIn: Flow<Boolean> = context.dataStore.data
    .map { preferences -> preferences[IS_LOGGED_IN_KEY] ?: false }

// Authenticate user
suspend fun login(username: String, password: String): Boolean {
    val preferences = context.dataStore.data.firstOrNull()
    return preferences?.get(USERNAME_KEY) == username &&
            preferences[PASSWORD_KEY] == password
}

// Logout
suspend fun logout() {
    context.dataStore.edit { preferences ->
        preferences[IS_LOGGED_IN_KEY] = false
    }
}

//----------------------------------------------------------------------------------------------

suspend fun saveDarkMode(isEnabled: Boolean) {
    context.dataStore.edit { preferences ->
        preferences[DARK_MODE_KEY] = isEnabled
    }
}

// Retrieve username
val usernameFlow: Flow<String> = context.dataStore.data
    .map { preferences -> preferences[USERNAME_KEY] ?: "" }

// Retrieve password
val passwordFlow: Flow<String> = context.dataStore.data
    .map { preferences -> preferences[PASSWORD_KEY] ?: "" }*/

/* // Read data
val usernameFlow: Flow<String> = context.dataStore.data
    .map { preferences -> preferences[USERNAME_KEY] ?: "Guest" }

val darkModeFlow: Flow<Boolean> = context.dataStore.data
    .map { preferences -> preferences[DARK_MODE_KEY] ?: false }*/
 */

}