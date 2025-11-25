package com.application.messagechat.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.application.messagechat.R
import com.application.messagechat.databinding.ActivityUpdateContactBinding
import com.google.firebase.firestore.FirebaseFirestore

class UpdateContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateContactBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_contact)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val name = intent.getStringExtra("name")
        if (name!!.contains(" ")){
            val contactName = name.split(" ")

            binding.updateUserName.setText(contactName[0])
            binding.updateUserLastName.setText(contactName[1])
        }else{
            binding.updateUserName.setText(name)
        }
        binding.updateUserPhoneNumber.setText(phoneNumber)

        binding.UpdateContactToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.buttonupdateContact.setOnClickListener {
            updateContact(phoneNumber!!)
            setResult(RESULT_OK)
            finish()
        }
    }

    fun updateContact(phoneNumber: String){
        sharedPreferences = getSharedPreferences("MyLoginInfo", MODE_PRIVATE)
        val loginId = sharedPreferences.getString("userId", null)
        val name = "${binding.updateUserName.text} ${binding.updateUserLastName.text}"

        val contact = firestore.collection("users")
            .document(loginId!!)
            .collection("contacts")

            contact.whereEqualTo("phoneNumber",phoneNumber)
            .get()
            .addOnSuccessListener {snapshots ->
               if (!snapshots.isEmpty){
                  for (document in snapshots.documents){
                      contact.document(document.id)
                          .update("name",name)
                  }
               }
            }
    }
}