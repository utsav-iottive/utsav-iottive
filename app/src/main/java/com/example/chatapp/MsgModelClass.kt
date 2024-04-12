package com.example.chatapp

data class MsgModelClass(
    val message: String? = null,
    val senderId: String? = null,
    val timestamp: Long? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val documentUri: String? = null,
    val audioUrl : String? = null,
    val contactUri:String? = null,
    var isSending: Boolean = false
)


