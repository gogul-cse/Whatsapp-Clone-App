package com.application.messagechat.activity

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.application.messagechat.R
import com.application.messagechat.databinding.ActivityAddNewContactBinding
import com.application.messagechat.model.UserContacts
import com.application.messagechat.util.ConvertImage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class AddNewContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewContactBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val firestore = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_new_contact)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.AddContactToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.buttonAddUser.setOnClickListener {
            addNewUserMessage()
        }


    }

    fun addNewUserMessage(){

        val userName = binding.addUserName.text.toString()
        val userLastName = binding.addUserLastName.text.toString()
        val fullName = "$userName $userLastName"
        val phoneNumber = binding.addUserPhoneNumber.text.toString()

        if (userName.isEmpty() || phoneNumber.isEmpty() || phoneNumber.length<10) {
            Toast.makeText(applicationContext,"Please Fill Name & Phone Number",
                Toast.LENGTH_LONG).show()
            return
        }
        sharedPreferences = getSharedPreferences("MyLoginInfo",MODE_PRIVATE)
        val loggedId = sharedPreferences.getString("userId",null)
        val id = phoneNumber
        if (loggedId != null){
            firestore.collection("users")
                .whereEqualTo("userPhoneNumber",phoneNumber)
                .get()
                .addOnSuccessListener { result->
                    firestore.collection("users")
                        .document(loggedId)
                        .collection("contacts")
                        .whereEqualTo("phoneNumber",phoneNumber)
                        .get()
                        .addOnSuccessListener { checkPhoneNumber->
                            if (checkPhoneNumber.isEmpty){
                                if (!result.isEmpty){
                                        val document = result.documents[0]
                                    val existUserId = document.id
                                    val profile = document.getString("profileImage")
                                    val contact = UserContacts(
                                        id = id, userId = loggedId, name = fullName,
                                        contactProfile = profile!!,
                                        lastMessageTime = Timestamp.now(),
                                        phoneNumber = phoneNumber,
                                        existingUser = existUserId)
                                    firestore.collection("users")
                                        .document(loggedId)
                                        .collection("contacts")
                                        .document(id)
                                        .set(contact)
                                }else{
                                    val contactProfile = ConvertImage.convertToString(BitmapFactory.decodeResource
                                        (resources,R.drawable.person_profile))
                                    val contact = UserContacts(
                                        id = id,
                                        userId = loggedId,
                                        name = fullName,
                                        contactProfile=contactProfile!!,
                                        lastMessageTime = Timestamp.now(),
                                        phoneNumber = phoneNumber,
                                        existingUser = null)
                                    firestore.collection("users")
                                        .document(loggedId)
                                        .collection("contacts")
                                        .document(id)
                                        .set(contact) }
                                binding.buttonAddUser.text = "Uploading.. please wait"
                                setResult(RESULT_OK)
                                finish()
                            }else{
                                Toast.makeText(this,"This contact already exist",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                            }
                    }


    }

}