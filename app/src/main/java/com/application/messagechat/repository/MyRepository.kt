package com.application.messagechat.repository

import android.content.Context
import android.widget.Toast
import com.application.messagechat.model.Messages
import com.application.messagechat.model.User
import com.application.messagechat.model.UserContacts
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class MyRepository {
    private var firestore = FirebaseFirestore.getInstance()

    suspend fun addUser(profileImage: String,userName: String,userPhoneNumber: String,email:String,context: Context){
        val user = User(
            id= userPhoneNumber,
            profileImage = profileImage,
            userName =  userName,
            userPhoneNumber =  userPhoneNumber,
            email =  email
        )
        firestore.collection("users")
            .document(userPhoneNumber)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(context,"User added successfully", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(context,"Error saving data", Toast.LENGTH_LONG).show()
            }
            .await()

    }

    // Contact
    suspend fun addContact(userId: String,contactName: String,contactProfile: String,
                           contactPhoneNumber: String,existingUser: String){

        val contact = UserContacts(
            id = contactPhoneNumber, userId = userId, name = contactName,
            contactProfile = contactProfile,
            lastMessageTime = Timestamp.now(),
            phoneNumber = contactPhoneNumber,
            existingUser = existingUser)

        firestore.collection("users")
            .document(userId)
            .collection("contacts")
            .document(contactPhoneNumber)
            .set(contact)
            .await()

    }

    suspend fun deleteContact(userId: String,contactId: String){
        val chatId = "${userId}_${contactId}"
        firestore.collection("users")
            .document(userId)
            .collection("contacts")
            .document(contactId)
            .delete()
            firestore.collection("chats")
                .document(chatId)
                .delete()
            .await()
    }

    suspend fun deleteAllContacts(userId: String){
        firestore.collection("users")
            .document(userId)
            .delete()
            .await()
    }


    // Messages
    suspend fun addMessage(senderId: String,reciverId: String,message: String){
        val senderChatId = "${senderId}_${reciverId}"
        val reciverChatId = "${reciverId}_${senderId}"
        val messageId = firestore.collection("messages").document().id
        val senderMessage = Messages(
            id = messageId, senderId = senderId, receiverId = reciverId, message = message,
            Timestamp.now()
        )
        val reciverMessage = Messages(
            id = messageId, senderId = reciverId, receiverId = senderId, message = message,
            Timestamp.now()
        )
        val chatRef = firestore.collection("chats").document(senderChatId)
        chatRef.set(mapOf("chatId" to senderChatId,
            "lastMessage" to message,
            "timeStamp" to Timestamp.now()), SetOptions.merge())

        chatRef.collection("messages")
            .document(messageId)
            .set(senderMessage)
            .await()


        val chatRefRevcive = firestore.collection("chats").document(reciverChatId)
        chatRef.set(mapOf("chatId" to reciverChatId,
            "lastMessage" to message,
            "timeStamp" to Timestamp.now()), SetOptions.merge())

        chatRefRevcive.collection("messages")
            .document(messageId)
            .set(reciverMessage)
            .await()
    }

    suspend fun deleteMessageForMe(){

    }

    suspend fun clearChat(chatId: String){
        val chatRef = firestore.collection("chats")
            .document(chatId)

        chatRef.collection("messages")
            .get()
            .addOnSuccessListener { messages ->
                for (message in messages.documents)
                    message.reference.delete()
            }
        chatRef.update(mapOf("lastMessage" to null, "timeStamp" to null))
    }

}