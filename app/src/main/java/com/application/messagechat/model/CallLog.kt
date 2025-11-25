package com.application.messagechat.model

import com.google.firebase.Timestamp

data class CallLog (
    val id: String = "",
    val contactId: String = "",
    val contactName: String = "",
    val contactPhoneNumber: String = "",
    val callType: String = "",
    val currentTime: Timestamp? = null
)