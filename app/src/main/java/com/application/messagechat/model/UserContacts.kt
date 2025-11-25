package com.application.messagechat.model

import com.google.firebase.Timestamp

data class UserContacts (
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val contactProfile:String = "",
    var lastMessageTime: Timestamp? = null,
    val phoneNumber: String = "",
    val existingUser: String? = null
)