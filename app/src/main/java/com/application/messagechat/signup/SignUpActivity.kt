package com.application.messagechat.signup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.application.messagechat.R
import com.application.messagechat.databinding.ActivitySignUpBinding
import com.application.messagechat.model.User
import com.application.messagechat.util.ControlPermission
import com.application.messagechat.util.ConvertImage
import com.application.messagechat.util.ImagePermissionHandler
import com.application.messagechat.util.MakeAction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    val firestore= FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var imageLauncher: ImagePermissionHandler
    private var selectImage: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_up)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding.signUpToolbar.setNavigationOnClickListener {
            finish()
        }
        imageLauncher = ImagePermissionHandler(this){bitmap, uri ->
            selectImage = bitmap
            binding.selectProfile.setImageBitmap(bitmap)
        }

        binding.selectProfile.setOnClickListener {
            imageLauncher.requestGalleryPermission()
        }

        binding.buttonSignUp.setOnClickListener {
            if (selectImage != null){
                errorCheck(selectImage!!)
            }else{
                val image = BitmapFactory.decodeResource(resources,R.drawable.person_profile)
                errorCheck(image)
            }
        }

    }

    fun errorCheck(selectImage:Bitmap){
        val name = binding.editTextUserName.text.toString()
        val phoneNumber = binding.editTextPhoneNumber.text.toString()
        val email = binding.editTextEmail.text.toString().lowercase()
        val password = binding.editTextPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()){
            toastMessage("Please fill everything")
            return
        }
        if (phoneNumber.length != 10){
            toastMessage("Enter valid phonenumber")
            return
        }
        if (password.length < 6){
            toastMessage("Password must be at least 6 character")
            return
        }
        if (password != confirmPassword){
            toastMessage("Password does not match")
            return
        }

        firestore.collection("users")
            .whereEqualTo("email",email)
            .get()
            .addOnSuccessListener { user ->
                if (!user.isEmpty){
                    toastMessage("Email already Exists")
                    return@addOnSuccessListener
                }
                firestore.collection("users")
                    .whereEqualTo("userPhoneNumber",phoneNumber)
                    .get()
                    .addOnSuccessListener { userphoneNumber ->
                        if (!userphoneNumber.isEmpty){
                            toastMessage("Phone number already exist")
                            return@addOnSuccessListener
                        }
                        binding.blurBackground.visibility = View.VISIBLE
                        binding.signUpProgressBar.isVisible = true
                        addUser(selectImage,name,phoneNumber,email,password)
                    }
            }
    }

    fun addUser(profile: Bitmap, name: String, phoneNumber: String, email: String, password: String){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                val imageAsString = ConvertImage.convertToString(profile)
                val userId = phoneNumber
                val user = User(
                    id= userId,
                    profileImage = imageAsString!!,
                    userName =  name,
                    userPhoneNumber =  phoneNumber,
                    email =  email
                )
                firestore.collection("users")
                    .document(userId)
                    .set(user)
                    .addOnSuccessListener {
                        toastMessage("User added successfully")
                        finish()
                    }
                    .addOnFailureListener {
                        toastMessage("Error saving data")
                    }
            }
            .addOnFailureListener {
                toastMessage("SignUp failed")
                binding.blurBackground.visibility = View.GONE
                binding.signUpProgressBar.isVisible = false
            }
    }

    fun toastMessage(message: String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
    }

}