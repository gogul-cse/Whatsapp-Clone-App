package com.application.messagechat.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.messagechat.repository.MyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyMessageViewModel: ViewModel() {

    private val repository = MyRepository()

    fun addUser(profileImage: String,userName: String,userPhoneNumber: String,email:String,context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(profileImage,userName,userPhoneNumber,email,context)
        }
    }

    fun addContact(userId: String,contactName: String,contactProfile: String,contactPhoneNumber: String,existingUser: String){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addContact(userId,contactName,contactProfile,contactPhoneNumber,existingUser)
        }
    }
}