package com.example.farmlinkapp.dialog

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.farmlinkapp.data.Message
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.databinding.NewMessageLayoutBinding
import com.example.farmlinkapp.messaging.SendMessage
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.UserAccountViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class NewMessageDialog(private val receiver: User): DialogFragment() {
    private lateinit var binding: NewMessageLayoutBinding

    private val userViewModel by viewModels<UserAccountViewModel>()
    private val sendViewModel by viewModels<SendMessage>()

    private var currentUser: User? = null
    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = NewMessageLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            userViewModel.user.collectLatest {
                when(it){
                    is Resource.Success ->{
                        currentUser = it.data
                        currentUserId = currentUser!!.userId
                    }
                    else -> Unit
                }

            }
        }

        val db = Firebase.firestore
        val usersRef = db.collection("user")
        val sellerId = receiver.userId

        usersRef.whereEqualTo("userId", sellerId).get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val sellerUser = document.toObject(User::class.java)
                binding.newMessageTitle.text = "Send a message to ${sellerUser!!.fullName}"
            }

        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "Error getting documents: ", exception)
        }


        binding.sendMessage.setOnClickListener {
            if (currentUser == receiver) {
                Toast.makeText(requireContext(), "You cannot message yourself!", Toast.LENGTH_SHORT).show()
            } else {

                val currentUser = currentUserId.toString()
                val message = binding.newMessageTextBox.text.toString().trim()
                val receivingId = receiver.userId

                val messageChat = Message(currentUser, message, receivingId, System.currentTimeMillis() / 1000)

                sendViewModel.performSendMessage(messageChat)
                Toast.makeText(requireContext(), "Message sent!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

    }
}