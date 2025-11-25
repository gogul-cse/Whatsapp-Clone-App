package com.application.messagechat.model


data class User   (
    val id: String = "",
    val deviceToken: String = "",
    val profileImage: String = "",
    val userName: String = "",
    val userPhoneNumber: String = "",
    val email: String = ""
)