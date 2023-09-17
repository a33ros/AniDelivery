package com.example.farmlinkapp.messaging

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.farmlinkapp.data.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SendMessage @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
): ViewModel() {

    fun performSendMessage(message: Message){

        val latestRef = firebaseDatabase.getReference("/latest-messages/${message.fromId}/${message.toId}")
        val latestToRef = firebaseDatabase.getReference("/latest-messages/${message.toId}/${message.fromId}")


        val sendRef = firebaseDatabase.getReference("/user-messages/${message.fromId}/${message.toId}").push()
        val receiverRef = firebaseDatabase.getReference("/user-messages/${message.toId}/${message.fromId}").push()


        sendRef.setValue(message)
        receiverRef.setValue(message)
        latestRef.setValue(message)
        latestToRef.setValue(message)

    }

}