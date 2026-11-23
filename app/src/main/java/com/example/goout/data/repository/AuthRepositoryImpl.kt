package com.example.goout.data.repository

import com.example.goout.data.FirestoreSchema
import com.example.goout.data.local.dao.TelemetryDao
import com.example.goout.data.local.datastore.UserPreferencesDataSource
import com.example.goout.data.remote.firebase.FirebaseProvider
import com.example.goout.domain.model.UserProfile
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebase: FirebaseProvider,
    private val preferences: UserPreferencesDataSource,
    private val telemetryDao: TelemetryDao,
) : AuthRepository {

    override val authState: Flow<AuthState> = run {
        val auth = firebase.auth
        if (auth == null) {
            flowOf(AuthState.Guest)
        } else {
            callbackFlow {
                val listener = FirebaseAuth.AuthStateListener { a ->
                    trySend(a.currentUser?.toProfile()?.let { AuthState.Authenticated(it) } ?: AuthState.Guest)
                }
                auth.addAuthStateListener(listener)
                awaitClose { auth.removeAuthStateListener(listener) }
            }
        }
    }

    override val currentUser: UserProfile?
        get() = firebase.auth?.currentUser?.toProfile()

    override suspend fun register(displayName: String, email: String, password: String): Result<UserProfile> =
        runCatching {
            val auth = firebase.auth ?: error("Firebase belum dikonfigurasi.")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("Registrasi gagal.")
            user.updateProfile(userProfileChangeRequest { this.displayName = displayName }).await()
            writeUserDocument(user.uid, displayName, email, isNew = true)
            UserProfile(user.uid, displayName, email)
        }

    override suspend fun login(email: String, password: String): Result<UserProfile> =
        runCatching {
            val auth = firebase.auth ?: error("Firebase belum dikonfigurasi.")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("Login gagal.")
            writeUserDocument(user.uid, user.displayName ?: "", user.email ?: email, isNew = false)
            user.toProfile()
        }

    /**
     * Logout policy (documented): telemetry is disabled, and this user's
     * PENDING telemetry rows are deleted (they can no longer be attributed
     * to an authenticated session). Pending reports are kept and synced later
     * because they were explicitly authored by the user while signed in.
     */
    override suspend fun logout() {
        val uid = firebase.auth?.currentUser?.uid
        preferences.setTelemetryEnabled(false)
        if (uid != null) {
            runCatching { telemetryDao.deletePendingForUser(uid) }
        }
        firebase.auth?.signOut()
    }

    private suspend fun writeUserDocument(uid: String, displayName: String, email: String, isNew: Boolean) {
        val firestore = firebase.firestore ?: return
        val data = mutableMapOf<String, Any>(
            "uid" to uid,
            "displayName" to displayName,
            "email" to email,
            "lastLoginAt" to FieldValue.serverTimestamp(),
            "accountStatus" to "active",
        )
        if (isNew) data["createdAt"] = FieldValue.serverTimestamp()
        // Best-effort profile write; auth session remains valid even if it fails offline.
        runCatching {
            firestore.collection(FirestoreSchema.USERS).document(uid)
                .set(data, com.google.firebase.firestore.SetOptions.merge()).await()
        }
    }

    private fun FirebaseUser.toProfile() = UserProfile(uid, displayName ?: "", email ?: "")
}
