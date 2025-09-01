@file:Suppress("DEPRECATION")

package com.example.repairstoremanager.util

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await

class GoogleAuthHelper(private val context: Context) {

    private val signInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(
                Scope("https://www.googleapis.com/auth/contacts"),
                Scope("https://www.googleapis.com/auth/contacts.readonly")
            )
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return signInClient.signInIntent
    }

    suspend fun handleSignInResult(data: Intent?): GoogleSignInAccount? {
        return try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            null
        }
    }

    fun getCurrentAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    suspend fun signOut() {
        signInClient.signOut().await()
    }

    companion object {
        const val RC_GOOGLE_SIGN_IN = 1001
    }
}