package com.application.messagechat.model

import com.google.firebase.Timestamp


data class Messages (
    val id: String = "",
    val senderId: String = "",
    val receiverId: String? = null,
    val message: String = "",
    val messageTime: Timestamp? = null
)