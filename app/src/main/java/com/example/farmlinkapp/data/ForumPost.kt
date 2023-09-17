package com.example.farmlinkapp.data

data class ForumPost(
    val postId: String,
    val userPostId: String,
    val content: String,
    val date: String
) {
    constructor():this("", "", "", "")
}
