package com.application.messagechat.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.application.messagechat.R
import com.application.messagechat.databinding.ActivitySettingsBinding
import com.application.messagechat.util.ConvertImage
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loggedPref: SharedPreferences
    private val firestore = FirebaseFirestore.getInstance()
    private var name: String? = null
    private var image: String? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == RESULT_OK){
                refreshPage()
            }

        }

        binding.switchTheme.setOnCheckedChangeListener { buttonView, isChecked ->

            sharedPreferences = this.getSharedPreferences("Dark Theme",MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            if (isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("switch",true)
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("switch",false)
            }
            editor.apply()

        }


        binding.themeToolBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.editContact->{
                    val intent = Intent(this@SettingsActivity, UpdateUserActivity::class.java)
                    intent.putExtra("name",name)
                    intent.putExtra("image",image)
                    activityResultLauncher.launch(intent)
                    true
                }else -> false
            }
        }

        binding.themeToolBar.setNavigationOnClickListener {
            finish()
        }
    }

    fun refreshPage(){
        loggedPref = getSharedPreferences("MyLoginInfo",MODE_PRIVATE)
        val logginId = loggedPref.getString("userId",null)
        if (logginId != null){
            firestore.collection("users")
                .whereEqualTo("id",logginId)
                .get()
                .addOnSuccessListener { user->
                    val document = user.documents[0]
                    image = document.getString("profileImage")
                    name = document.getString("userName")
                    binding.userProfileImage.setImageBitmap(ConvertImage.convertToBitmap(image))
                    binding.textViewName.text = name
                    binding.textViewPhoneNumber.text = document.getString("userPhoneNumber")
                    binding.textViewEmail.text = document.getString("email")
                }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPage()
        sharedPreferences = this.getSharedPreferences("Dark Theme",MODE_PRIVATE)
        val isDark = sharedPreferences.getBoolean("switch",false)
        binding.switchTheme.isChecked = isDark
    }

}