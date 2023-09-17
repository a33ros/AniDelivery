package com.example.farmlinkapp.fragments.mainmenufrags.forum_utils

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
import com.example.farmlinkapp.data.PostComment
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.databinding.CommentLayoutBinding
import com.example.farmlinkapp.databinding.ForumPostLayoutBinding
import com.example.farmlinkapp.databinding.FragmentForumPostViewBinding
import com.example.farmlinkapp.fragments.home_buttonfragments.ForumFragment
import com.example.farmlinkapp.viewmodel.ForumViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@AndroidEntryPoint
class ForumPostViewFragment : Fragment() {
    private lateinit var binding: FragmentForumPostViewBinding

    private val viewModel by viewModels<ForumViewModel>()

    val firestore = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val ref = FirebaseDatabase.getInstance("https://farmlink-app-b781b-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private var postUser: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForumPostViewBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.commentsView.adapter = adapter
        binding.commentsView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_forumPostViewFragment_pop)
        }

        postUser = arguments?.getString("ForumPostUser") ?: throw IllegalArgumentException("Post not found in arguments bundle")

        val postDetails = ref.getReference("/forum-posts/${postUser}")
        postDetails.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(ForumPost::class.java)

                binding.postContent.text = post?.content
                binding.postDate.text = post?.date

                lifecycleScope.launch {

                    firestore.collection("user").whereEqualTo( "userId", post?.userPostId)
                        .get().addOnSuccessListener { querySnapshot ->
                            for (document in querySnapshot.documents) {
                                val userPost = document.toObject(User::class.java)
                                Glide.with(requireContext())
                                    .load(userPost?.imagePath)
                                    .error(R.mipmap.profile_mini_round)
                                    .into(binding.forumPostViewImage)
                                binding.postUser.text = userPost?.fullName
                            }
                        }.addOnFailureListener { exception ->
                            Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        })

        binding.commentButton.setOnClickListener {
            binding.commentButton.startAnimation()
            val commentContent = binding.postCommentContent.text.toString().trim()
            if (commentContent.isEmpty() || commentContent.isBlank()){
                Toast.makeText(requireContext(), "Content is empty, please review your post", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val userId = auth.uid.toString()
                    val commentId = UUID.randomUUID().toString()
                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    val date = LocalDate.now().toString()
                    val timestamp0 = currentDateTime.format(formatter)
                    val timestamp = "${date}, $timestamp0"

                    val postComment = userId?.let { it1 ->
                        PostComment(
                            commentId,
                            commentContent,
                            timestamp,
                            it1
                        )
                    }

                    if (postComment != null) {
                        viewModel.postComment(postUser!!, postComment)
                        binding.postCommentContent.text?.clear()
                        binding.commentButton.revertAnimation()
                    }

                }
            }
        }

        listenForComments()

    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    val latestCommentMap = HashMap<String, PostComment>()

    private fun listenForComments() {
        val commentRef = ref.getReference("/forum-posts/${postUser}/comments")
        commentRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val commentPost = snapshot.getValue(PostComment::class.java) ?: return
                latestCommentMap[snapshot.key!!] = commentPost
                refreshRecyclerViewPost()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val commentPost = snapshot.getValue(PostComment::class.java) ?: return
                latestCommentMap[snapshot.key!!] = commentPost
                refreshRecyclerViewPost()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }

        })
    }

    class LatestCommentEntries(val forumComment: PostComment): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val binding = CommentLayoutBinding.bind(viewHolder.itemView)

            binding.commentContent.text = forumComment.commentContent
            binding.commentDate.text = forumComment.timestamp

            val firestore1 = Firebase.firestore
            firestore1.collection("user").whereEqualTo("userId", forumComment.userCommentId)
                .get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val userPost = document.toObject(User::class.java)
                        Glide.with(viewHolder.itemView.context)
                            .load(userPost?.imagePath)
                            .error(R.mipmap.profile_mini_round)
                            .into(binding.forumPostImage)
                        binding.userCommentName.text = userPost?.fullName
                    }
                }.addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                }

        }
        override fun getLayout(): Int {
            return R.layout.comment_layout
        }
    }

    fun refreshRecyclerViewPost(){
        adapter.clear()
        latestCommentMap.values.forEach {
            adapter.add(ForumPostViewFragment.LatestCommentEntries(it))
        }
    }

}