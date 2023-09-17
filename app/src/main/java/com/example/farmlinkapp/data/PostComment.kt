package com.example.farmlinkapp.data

data class PostComment(
    val commentId: String,
    val commentContent: String,
    val timestamp: String,
    val userCommentId: String
) {
    constructor(): this("", "", "", "")
}
