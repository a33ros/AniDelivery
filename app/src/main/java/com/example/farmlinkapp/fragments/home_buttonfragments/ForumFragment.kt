package com.example.farmlinkapp.fragments.home_buttonfragments

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.ForumPost
import com.example.farmlinkapp.data.Message
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.databinding.ForumPostLayoutBinding
import com.example.farmlinkapp.databinding.FragmentForumBinding
import com.example.farmlinkapp.databinding.MessageUserLayoutBinding
import com.example.farmlinkapp.viewmodel.ForumViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@AndroidEntryPoint
class ForumFragment : Fragment() {
    private lateinit var binding: FragmentForumBinding

    private val viewModel by viewModels<ForumViewModel>()

    val auth = FirebaseAuth.getInstance()
    val firestore = Firebase.firestore
    val realtimeDatabase = FirebaseDatabase.getInstance("https://farmlink-app-b781b-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForumBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.postView.adapter = adapter
        binding.postView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        listenForumPosts()

        adapter.setOnItemClickListener { item, view ->
            val row = item as LatestForumEntries
            val b = Bundle().apply {
                putString("ForumPostUser", row.postId)
            }
            findNavController().navigate(R.id.action_forumFragment_to_forumPostViewFragment, b)
        }

        binding.postButton.setOnClickListener {
            if (binding.forumPostContent.text?.isBlank() == true || binding.forumPostContent.text?.isEmpty() == true){
                Toast.makeText(requireContext(), "Content is empty, please review your post", Toast.LENGTH_SHORT).show()
            } else {
                val postId = UUID.randomUUID().toString()
                val content = binding.forumPostContent.text.toString().trim()
                val date = LocalDate.now().toString()
                lifecycleScope.launch {
                    val userPostId = auth.uid
                    val forumPostData = userPostId?.let { it1 ->
                        ForumPost(
                            postId,
                            it1,
                            content,
                            date
                        )
                    }
                    if (forumPostData != null) {
                        viewModel.performPostForum(forumPostData)
                        binding.forumPostContent.text?.clear()
                    }
                    }
                }
            }
        }

    val latestPostMap = HashMap<String, ForumPost>()

    private fun listenForumPosts() {
        val ref = realtimeDatabase.getReference("/forum-posts")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val forumPost = snapshot.getValue(ForumPost::class.java) ?: return
                latestPostMap[snapshot.key!!] = forumPost
                refreshRecyclerViewPost()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val forumPost = snapshot.getValue(ForumPost::class.java) ?: return
                latestPostMap[snapshot.key!!] = forumPost
                refreshRecyclerViewPost()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        })
    }

    fun refreshRecyclerViewPost(){
        adapter.clear()
        latestPostMap.values.forEach {
            adapter.add(LatestForumEntries(it))
        }
    }

    class LatestForumEntries(val forumEntry: ForumPost): Item<GroupieViewHolder>(){
        var userPost: User? = null
        var postId = forumEntry.postId

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val binding = ForumPostLayoutBinding.bind(viewHolder.itemView)

            binding.forumContent.text = forumEntry.content
            binding.forumPostDate.text = forumEntry.date

            val reference = FirebaseDatabase.getInstance("https://farmlink-app-b781b-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/forum-posts/${forumEntry.postId}/comments")
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    if (count > 1){
                        binding.forumComments.text = "$count Comments"
                    } else {
                        binding.forumComments.text = "$count Comment"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //
                }
            })
            val firestore1 = Firebase.firestore
            firestore1.collection("user").whereEqualTo("userId", forumEntry.userPostId)
                .get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        userPost = document.toObject(User::class.java)
                        Glide.with(viewHolder.itemView.context)
                            .load(userPost?.imagePath)
                            .error(R.mipmap.profile_mini_round)
                            .into(binding.forumPostImage)
                        binding.forumPostUser.text = userPost?.fullName
                    }
                }.addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                }

        }
        override fun getLayout(): Int {
            return R.layout.forum_post_layout
        }
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

}

