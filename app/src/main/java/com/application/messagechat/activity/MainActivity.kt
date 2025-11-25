package com.application.messagechat.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.application.messagechat.R
import com.application.messagechat.adapter.ViewPagerAdapter
import com.application.messagechat.databinding.ActivityMainBinding
import com.application.messagechat.signup.LoginActivity
import com.application.messagechat.util.CallPermissionHandler
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var callLauncher: CallPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        callLauncher = CallPermissionHandler(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val adapter = ViewPagerAdapter(this)
        binding.viewPager2.adapter = adapter

        AppCompatActivity()
        menuInflater.inflate(R.menu.main_menu,binding.materialToolbar.menu)

        binding.materialToolbar.setOnMenuItemClickListener { menuItem ->

            when(menuItem.itemId){
                R.id.settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.logoutAccount ->{
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Logout")
                        .setMessage("Do you want to Logout.")
                        .setCancelable(false)
                        .setPositiveButton("Yes"){_,_->
                            sharedPreferences = this.getSharedPreferences("MyLoginInfo",MODE_PRIVATE)
                            sharedPreferences.edit().clear().apply()
                            val activity = Intent(this, LoginActivity::class.java)
                            startActivity(activity)
                            finish()
                        }
                        .setNegativeButton("No",null)
                        .show()
                    true
                }
                R.id.imageViewDeleteAllChats ->{
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Delete")
                        .setMessage("You Want to Clear All chats. " +
                                "\nBy Long Press on particular chat to delete person's chat")
                        .setCancelable(false)
                        .setPositiveButton("Yes"){_,_->
                            deleteAllContacts()
                        }
                        .setNegativeButton("No",null)
                        .show()
                    true
                }
                R.id.clearCallLog->{
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Delete")
                        .setMessage("You Want to Clear All call.")
                        .setCancelable(false)
                        .setPositiveButton("Yes"){_,_->
                            clearCallLog()
                        }
                        .setNegativeButton("No",null)
                        .show()
                    true
                }
                else -> false
            }

        }
        binding.profileImageLayout.setOnClickListener {
            binding.profileImageLayout.isGone = true
        }

        TabLayoutMediator(binding.tabLayout,binding.viewPager2){tab,position ->
            when (position){
                0-> {
                    tab.text = "Chats"
                }
                1 ->{
                    tab.text = "Status"
                }
                2 -> {
                    tab.text = "Calls"
                }
            }
        }.attach()

    }

    fun deleteAllContacts(){
        sharedPreferences = this.getSharedPreferences("MyLoginInfo",MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId",null)

        firestore.collection("users")
            .document(userId!!)
            .collection("contacts")
            .get()
            .addOnSuccessListener {snapshots ->
                for (document in snapshots.documents){
                    val message = document.reference.collection("messages")
                    message.get().addOnSuccessListener { messagesnap->
                        for (msginCon in messagesnap.documents )
                            msginCon.reference.delete()
                    }
                    document.reference.delete()
                }
            }
    }

    fun clearCallLog(){
        sharedPreferences = this.getSharedPreferences("MyLoginInfo",MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId",null)

        firestore.collection("users")
            .document(userId!!)
            .collection("callLog")
            .get()
            .addOnSuccessListener { snapshots ->
                for (document in snapshots.documents){
                    document.reference.delete()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences = getSharedPreferences("Dark Theme",MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("switch",false)
        if (isDarkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

    }
    fun profileImageView(visible: Boolean,profile: Bitmap,contactId: String){
        sharedPreferences = this.getSharedPreferences("MyLoginInfo",MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId",null)
        binding.profileImageLayout.isVisible = visible
        binding.profileImageView.setImageBitmap(profile)

        firestore.collection("users").document(userId!!)
            .collection("contacts").document(contactId).get()
            .addOnSuccessListener { snapshot ->
                binding.profileCallButton.setOnClickListener {
                    callLauncher.setphoneNumber(snapshot.getString("phoneNumber")!!)
                    callLauncher.requestCallPermission()
                }
                val contactProfile = snapshot.getString("contactProfile")
                val existId = snapshot.getString("existingUser")
                binding.profileMsgButton.setOnClickListener {
                    val intent = Intent(this, MyMessagesChatActivity::class.java)
                    intent.putExtra("contactId",contactId)
                    intent.putExtra("existUserId",existId)
                    intent.putExtra("contactNumber",contactId)
                    intent.putExtra("image",contactProfile)
                    startActivity(intent)
                    binding.profileImageLayout.isVisible = false
                }
            }
    }
    fun delechat(visible: Boolean){
        binding.materialToolbar.menu.findItem(R.id.imageViewDeleteAllChats)?.isVisible = visible
    }
    fun clearCall(visible: Boolean){
        binding.materialToolbar.menu.findItem(R.id.clearCallLog).isVisible = visible
    }
}