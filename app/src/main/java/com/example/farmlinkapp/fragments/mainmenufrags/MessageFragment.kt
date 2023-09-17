package com.example.farmlinkapp.fragments.mainmenufrags

import android.content.ContentValues.TAG
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.Message
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.databinding.FragmentMessageBinding
import com.example.farmlinkapp.databinding.MessageUserLayoutBinding
import com.example.farmlinkapp.viewmodel.UserAccountViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessageFragment: Fragment() {

    private lateinit var binding: FragmentMessageBinding

    private val viewModel by viewModels<UserAccountViewModel>()

    val firebaseDatabase = FirebaseDatabase.getInstance("https://farmlink-app-b781b-default-rtdb.asia-southeast1.firebasedatabase.app/")


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMessageBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recentMessages.adapter = adapter
        binding.recentMessages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        listenLatestMessages()

        adapter.setOnItemClickListener { item, view ->
            val row = item as LatestMessageRows
            val b = Bundle().apply {
                putParcelable("User", row.chatPartnerUser)
            }
            findNavController().navigate(R.id.action_messageFragment_to_chatFragment, b)
        }
    }

    val latestMessagesMap = HashMap<String, Message>()

    private fun listenLatestMessages() {
        val auth = FirebaseAuth.getInstance()
        val ref = firebaseDatabase.getReference("/latest-messages/${auth.uid}")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val messageDetails = snapshot.getValue(Message::class.java) ?: return
                    latestMessagesMap[snapshot.key!!] = messageDetails
                    refreshRecyclerViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val messageDetails = snapshot.getValue(Message::class.java) ?: return
                    latestMessagesMap[snapshot.key!!] = messageDetails
                    refreshRecyclerViewMessages()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRows(it))
        }
    }

    class LatestMessageRows(private val chatMessage: Message): Item<GroupieViewHolder>(){
        var chatPartnerUser: User? = null
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val binding = MessageUserLayoutBinding.bind(viewHolder.itemView)
            binding.lastMessageView.text = chatMessage.text

            val ref = FirebaseAuth.getInstance().uid
            val chatPartnerId: String
            if (chatMessage.fromId == ref) {
                chatPartnerId = chatMessage.toId
            } else {
                chatPartnerId = chatMessage.fromId
            }

            val db = Firebase.firestore
            val usersRef = db.collection("user")

            usersRef.whereEqualTo("userId", chatPartnerId).get().addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    chatPartnerUser = document.toObject(User::class.java)
                    Glide.with(viewHolder.itemView.context)
                        .load(chatPartnerUser?.imagePath)
                        .error(R.mipmap.profile_mini_round)
                        .into(binding.messageProfileView)
                    binding.userView.text = chatPartnerUser?.fullName
                }
            }.addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }

        }
        override fun getLayout(): Int {
            return R.layout.message_user_layout
        }
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

}