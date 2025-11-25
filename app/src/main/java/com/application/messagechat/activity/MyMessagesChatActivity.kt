package com.application.messagechat.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.messagechat.R
import com.application.messagechat.adapter.MyMessageChatsAdapter
import com.application.messagechat.databinding.ActivityMyMessagesChatBinding
import com.application.messagechat.model.CallLog
import com.application.messagechat.model.Messages
import com.application.messagechat.util.CallPermissionHandler
import com.application.messagechat.util.CameraPermissionHandler
import com.application.messagechat.util.ControlPermission
import com.application.messagechat.util.ConvertImage
import com.application.messagechat.util.MakeAction
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.api.services.drive.DriveScopes
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MyMessagesChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyMessagesChatBinding
    private lateinit var myMessageChatsAdapter: MyMessageChatsAdapter
    private var currentPhoneNumber: String? = null
    private var isEditing = false
    private var messageBeingEdited: Messages? = null
    private lateinit var sharedPreferences: SharedPreferences
    private val firestore = FirebaseFirestore.getInstance()
    private var textId: String? = null
    private var contactName: String? = null
    private var contactPhoneNumber: String? = null
    private lateinit var googleSignIn: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectDocLauncher: ActivityResultLauncher<String>
    private lateinit var callLauncher: CallPermissionHandler
    private lateinit var cameraLauncher: CameraPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_messages_chat)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setUpGoogleDrive()
        callLauncher = CallPermissionHandler(this)
        cameraLauncher = CameraPermissionHandler(this)

        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ result ->
                if (result.resultCode == Activity.RESULT_OK){
                    GoogleSignIn.getLastSignedInAccount(this)?.let {
                        Toast.makeText(this, "Signed in as ${it.email}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        val toolbar = binding.chatToolBar
        val contactId = intent.getStringExtra("contactId")

        sharedPreferences = getSharedPreferences("MyLoginInfo", MODE_PRIVATE)
        val loginId = sharedPreferences.getString("userId", null)
        var reciverId = intent.getStringExtra("existUserId")
        val image = intent.getStringExtra("image")

        if (reciverId == null) {
            reciverId = contactId
        }

        val chatId =
            if (loginId!! < reciverId!!) "${loginId}_${reciverId}" else "${reciverId}_${loginId}"
        Log.d("message", "chatId${chatId}")

        selectDocLauncher = MakeAction.attachementUpload(this){driveUrl, mimeType ->
            if (driveUrl!=null){
                if (GoogleSignIn.getLastSignedInAccount(this) == null) {
                    promptSignIn()
                    return@attachementUpload
                }
                sendMessage(loginId,reciverId,chatId,driveUrl)
            }
        }

        myMessageChatsAdapter = MyMessageChatsAdapter(
            toolbar, this,
            { messages,messageId ->
                binding.editTextMessage.setText(messages.message)
                binding.editTextMessage.setSelection(binding.editTextMessage.text.length)// to move cursor of text to last
                binding.editTextMessage.requestFocus()
                isEditing = true
                textId = messageId
                messageBeingEdited = messages
            },
            { messages,messageId ->
                deleteMessage(chatId,messageId,loginId,contactId!!)
            }, loginId)

        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMessages.adapter = myMessageChatsAdapter
        binding.layoutToolBarChat.imageViewChatProfile.setImageBitmap(ConvertImage.convertToBitmap(image))

        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("messageTime", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { it.toObject(Messages::class.java) }
                    //updated UI
                    myMessageChatsAdapter.setMessages(messages)
                    if (messages.isNotEmpty()) {
                        binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
                    }

                }
            }

        binding.sendMessage.setOnClickListener {
            val messagesSent = binding.editTextMessage.text.toString()
            if (messagesSent.isNotEmpty()) {
                if (isEditing && messageBeingEdited != null) {
                    myMessageChatsAdapter.clearSelection()
                    updateMessage(chatId,messagesSent)
                    isEditing = false
                    messageBeingEdited = null
                } else { //to add msg in DB
                    sendMessage(loginId, reciverId, chatId, messagesSent)
                }
                binding.editTextMessage.text.clear()
            }

        }


        if (loginId != null) {
            firestore.collection("users")
                .document(loginId)
                .collection("contacts")
                .whereEqualTo("id", contactId)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty && result.documents.isNotEmpty()) {
                        val document = result.documents[0]
                        contactName = (document.getString("name"))
                        contactPhoneNumber = document.getString("phoneNumber")
                        binding.layoutToolBarChat.textViewChatSenderName.text = contactName
                        currentPhoneNumber = contactPhoneNumber
                        callLauncher.setphoneNumber(currentPhoneNumber!!)
                    }
                }

        }
        binding.layoutToolBarChat.imageViewPhone.setOnClickListener {
            callLauncher.requestCallPermission()
            val callId = firestore.collection("callLog").document().id
            val callLog = CallLog(
                id = callId,
                contactId = contactId!!,
                contactName = contactName!!,
                contactPhoneNumber = contactPhoneNumber!!,
                callType = "Outgoing",
                currentTime = Timestamp.now()
            )
            firestore.collection("users")
                .document(loginId)
                .collection("callLog")
                .document(callId)
                .set(callLog)
        }

        binding.layoutToolBarChat.textViewChatSenderName.setOnClickListener {
            val intent = Intent(this, ViewContactInfoActivity::class.java)
            intent.putExtra("contactId", contactId)
            intent.putExtra("userId", loginId)
            intent.putExtra("image",image)
            startActivity(intent)
        }

        binding.chatToolBar.setNavigationOnClickListener {
            finish()
        }


        binding.chatToolBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.deleteAllMessage -> {

                    AlertDialog.Builder(this)
                        .setTitle("Delete")
                        .setMessage("Do You Want to Clear All message")
                        .setCancelable(false)
                        .setPositiveButton("Yes") { _, _ ->
                            clearAllChat(chatId)
                        }
                        .setNegativeButton("No", null)
                        .show()
                    return@setOnMenuItemClickListener true
                }

                else -> return@setOnMenuItemClickListener false

            }
        }

        binding.addAttachment.setOnClickListener {
            showAttachment()
        }

        binding.takePhoto.setOnClickListener {
            cameraLauncher.requestCameraPermission()
        }

        binding.micEnable.setOnClickListener {
            if (ControlPermission.micPermission(this)){
                MakeAction.recoreAudio(this,binding.micEnable)
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),3)
            }
        }
    }

    fun sendMessage(loginId: String, reciverId: String, chatId: String, messagesSent: String) {

        val messageId = firestore.collection("messages").document().id
        val messages = Messages(
            id = messageId, senderId = loginId, receiverId = reciverId, message = messagesSent,
            Timestamp.now()
        )
        val chatRef = firestore.collection("chats").document(chatId)

            chatRef.set(mapOf(
                    "chatId" to chatId,
                    "lastMessage" to messagesSent,
                    "timeStamp" to Timestamp.now()
                ), SetOptions.merge()
            )

            chatRef.collection("messages")
            .document(messageId)
            .set(messages)
                .addOnSuccessListener {
                    firestore.collection("users").document(loginId)
                        .collection("contacts").document(reciverId)
                        .update("lastMessageTime", Timestamp.now())

                    firestore.collection("users").document(reciverId)
                        .collection("contacts").document(loginId)
                        .update("lastMessageTime", Timestamp.now())
                }

    }

    fun updateMessage(chatId: String,message: String){
        val chat = firestore.collection("chats").document(chatId)
            chat.collection("messages")
            .document(textId!!)
            .update("message",message)
        chat.update("lastMessage",message)
    }

    fun deleteMessage(chatId: String,messageId: String,userId: String,contactId: String){
        val chatRef = firestore.collection("chats").document(chatId)
            .collection("messages")
        chatRef.document(messageId)
        .delete()
            .addOnSuccessListener {
                chatRef.orderBy("messageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener {queryDocumentSnapshots ->
                        val lastMessage = queryDocumentSnapshots.documents.firstOrNull()
                        val messageRef = firestore.collection("chats").document(chatId)
                        val userRef = firestore.collection("users").document(userId)
                            .collection("contacts").document(contactId)
                        if (lastMessage != null){
                            val lastText = lastMessage.getString("message")?: ""
                            val lastMsgTime = lastMessage.getTimestamp("messageTime")
                            messageRef.update(mapOf("lastMessage" to lastText,"timeStamp" to lastMsgTime))
                            userRef.update("lastMessageTime",lastMsgTime)
                        }else{
                            messageRef.update(mapOf("lastMessage" to "","timeStamp" to ""))
                            userRef.update("lastMessageTime","")
                        }
                    }
            }
    }

    fun clearAllChat(chatId: String) {
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

    fun setUpGoogleDrive(){
        val googleSIgnOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        googleSignIn = GoogleSignIn.getClient(this,googleSIgnOption)
    }

    fun promptSignIn(){
        val signInIntent = googleSignIn.signInIntent
        signInLauncher.launch(signInIntent)
    }
    fun showAttachment(){
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            promptSignIn()
            return
        }
        val attachment = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.attachment_files,null)

        val image = view.findViewById<LinearLayout>(R.id.select_image)
        val video = view.findViewById<LinearLayout>(R.id.select_video)
        val doc = view.findViewById<LinearLayout>(R.id.select_doc)

        image.setOnClickListener {
            attachment.dismiss()
            selectDocLauncher.launch("image/*")
        }
        video.setOnClickListener {
            attachment.dismiss()
            selectDocLauncher.launch("video/*")
        }
        doc.setOnClickListener {
            attachment.dismiss()
            selectDocLauncher.launch("*/*")
        }
        attachment.setContentView(view)
        attachment.show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 3){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                MakeAction.recoreAudio(this,binding.micEnable)
            }else{
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)){
                    Toast.makeText(this, "Enable MicroPhone permission in Settings", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}