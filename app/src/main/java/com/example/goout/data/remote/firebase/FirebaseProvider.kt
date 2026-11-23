package com.example.goout.data.remote.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides Firebase services, degrading gracefully when the project has not
 * been configured yet (no google-services.json): [auth]/[firestore] are null
 * and the app keeps working in offline/guest mode on cached Room data.
 */
@Singleton
class FirebaseProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val isAvailable: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    val auth: FirebaseAuth?
        get() = if (isAvailable) FirebaseAuth.getInstance() else null

    val firestore: FirebaseFirestore?
        get() = if (isAvailable) FirebaseFirestore.getInstance() else null
}
