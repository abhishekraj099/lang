package com.example.lang.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isGuest: Boolean,
)

class AuthRepository(
) {
    private val auth: FirebaseAuth? by lazy { runCatching { FirebaseAuth.getInstance() }.getOrNull() }
    private val firestore: FirebaseFirestore? by lazy { runCatching { FirebaseFirestore.getInstance() }.getOrNull() }

    val currentUser: AuthUser?
        get() = auth?.currentUser?.toAuthUser()

    fun observeAuthUser(): Flow<AuthUser?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { current ->
            trySend(current.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(firebaseAuth.currentUser?.toAuthUser())
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String): AuthUser {
        val firebaseAuth = requireAuth()
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Firebase sign in returned no user.")
        ensureUserDocument(user, user.displayName ?: user.email?.substringBefore("@") ?: "Learner")
        return user.toAuthUser()
    }

    suspend fun signUp(email: String, password: String, displayName: String): AuthUser {
        val firebaseAuth = requireAuth()
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Firebase sign up returned no user.")
        ensureUserDocument(user, displayName.ifBlank { email.substringBefore("@") })
        return user.toAuthUser()
    }

    suspend fun signInWithGoogle(idToken: String): AuthUser {
        val firebaseAuth = requireAuth()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val user = result.user ?: error("Firebase Google sign-in returned no user.")
        ensureUserDocument(user, user.displayName ?: user.email?.substringBefore("@") ?: "Learner")
        return user.toAuthUser()
    }

    fun signOut() {
        auth?.signOut()
    }

    private fun requireAuth(): FirebaseAuth = auth ?: error("Firebase Auth is not initialized. Check google-services.json and Firebase setup.")

    private suspend fun ensureUserDocument(user: FirebaseUser, displayName: String) {
        val firebaseFirestore = firestore ?: return
        val profile = mapOf(
            "userId" to user.uid,
            "email" to user.email.orEmpty(),
            "displayName" to displayName,
            "selectedLanguages" to listOf("japanese"),
            "activeLanguage" to "japanese",
            "createdAt" to System.currentTimeMillis(),
            "settings" to mapOf(
                "dailyGoalMinutes" to 10,
                "notificationsEnabled" to true,
                "themeMode" to "system",
            ),
        )
        firebaseFirestore.collection("users").document(user.uid).set(profile, SetOptions.merge()).await()
    }

    private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        isGuest = false,
    )
}
