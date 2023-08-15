package com.example.firebasekotlin.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstanceIDService : FirebaseMessagingService() {

    override fun onNewToken(refreshedToken: String) {
        super.onNewToken(refreshedToken)

        if (FirebaseAuth.getInstance().currentUser != null) {
            tokenVeriTabaninaKaydet(refreshedToken)
        }
    }

    private fun tokenVeriTabaninaKaydet(refreshedToken: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val ref = FirebaseDatabase.getInstance().reference
                .child("kullanici")
                .child(uid)
                .child("mesaj_token")
            ref.setValue(refreshedToken)
        }
    }
}
