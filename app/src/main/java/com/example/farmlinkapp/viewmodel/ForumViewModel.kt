package com.example.farmlinkapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.farmlinkapp.data.ForumPost
import com.example.farmlinkapp.data.PostComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ForumViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
): ViewModel() {

    fun performPostForum(forumPost: ForumPost){

        firebaseDatabase.getReference("/forum-posts/${forumPost.postId}").setValue(forumPost)

    }

    fun postComment(postId: String ,postComment: PostComment) {
        firebaseDatabase.getReference("/forum-posts/${postId}/comments").push().setValue(postComment)
    }

}