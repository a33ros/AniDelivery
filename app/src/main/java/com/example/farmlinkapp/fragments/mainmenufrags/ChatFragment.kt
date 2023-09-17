package com.example.farmlinkapp.fragments.mainmenufrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.Message
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.databinding.FragmentChatBinding
import com.example.farmlinkapp.databinding.MessageChatsLayoutChatPartnerBinding
import com.example.farmlinkapp.databinding.MessageChatsLayoutUserBinding
import com.example.farmlinkapp.messaging.SendMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private val viewModel by viewModels<SendMessage>()

    private val firebaseDatabase = FirebaseDatabase.getInstance("https://farmlink-app-b781b-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private lateinit var binding: FragmentChatBinding
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = arguments?.getParcelable("User") ?: throw IllegalArgumentException("User not found in arguments bundle")

        binding.messagesView.adapter = adapter
        binding.messagesView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        listenForMessages()

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_chatFragment_pop)
        }

        binding.sendMessage.setOnClickListener {
            val auth = FirebaseAuth.getInstance().uid

            val messageText = binding.newMessageTextBox.text.toString().trim()
            val fromId = auth.toString()
            val toId = user.userId

            val message = Message(fromId, messageText, toId, System.currentTimeMillis() / 1000)
            viewModel.performSendMessage(message)

            binding.newMessageTextBox.text?.clear()
            binding.messagesView.scrollToPosition(adapter.itemCount - 0)
        }

        binding.apply {
            Glide.with(requireView()).load(user.imagePath).error(R.mipmap.profile_mini_round).into(binding.userProfile)
            userNameMessage.text = user.fullName
        }
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    class ChatFromItem(val text: String): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val binding = MessageChatsLayoutChatPartnerBinding.bind(viewHolder.itemView)

            binding.receiverText.text = text
        }

        override fun getLayout(): Int {
            return R.layout.message_chats_layout_chat_partner
        }
    }

    class ChatToItem(val text: String): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val binding = MessageChatsLayoutUserBinding.bind(viewHolder.itemView)

            binding.senderText.text = text
        }

        override fun getLayout(): Int {
            return R.layout.message_chats_layout_user
        }
    }

    private fun listenForMessages(){
        val auth = FirebaseAuth.getInstance().uid
        val fromId = auth.toString()
        val toId = user.userId
        val ref = firebaseDatabase.getReference("/user-messages/${fromId}/${toId}")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(Message::class.java)

                if (chatMessage != null){
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatToItem(chatMessage.text))
                    }else{
                        adapter.add(ChatFromItem(chatMessage.text))
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }
}