package com.example.repairstoremanager.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.people.v1.PeopleService
import com.google.api.services.people.v1.model.Person
import com.google.api.services.people.v1.model.Name
import com.google.api.services.people.v1.model.PhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleContactsRepository(private val context: Context) {

    private fun getPeopleService(account: GoogleSignInAccount): PeopleService {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(
                "https://www.googleapis.com/auth/contacts",
                "https://www.googleapis.com/auth/contacts.readonly"
            )
        )
        credential.selectedAccount = account.account!!

        return PeopleService.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Repair Store Manager").build()
    }

    suspend fun saveContactToGmail(
        account: GoogleSignInAccount,
        name: String,
        phoneNumber: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val peopleService = getPeopleService(account)

            val contact = Person().apply {
                names = listOf(
                    Name().apply {
                        givenName = name
                        displayName = name
                    }
                )
                phoneNumbers = listOf(
                    PhoneNumber().apply {
                        value = phoneNumber
                        type = "mobile"
                    }
                )
            }

            val createRequest = peopleService.people().createContact(contact)
            createRequest.execute()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getContactsFromGmail(account: GoogleSignInAccount): List<Pair<String, String>> =
        withContext(Dispatchers.IO) {
            try {
                val peopleService = getPeopleService(account)

                val response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("names,phoneNumbers")
                    .setPageSize(100)
                    .execute()

                response.connections?.mapNotNull { person ->
                    val name = person.names?.firstOrNull()?.displayName ?: "Unknown"
                    val phone = person.phoneNumbers?.firstOrNull()?.value ?: ""
                    if (phone.isNotBlank() && name != "Unknown") name to phone else null
                } ?: emptyList()

            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
}