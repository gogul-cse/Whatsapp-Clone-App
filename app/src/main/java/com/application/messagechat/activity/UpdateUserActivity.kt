package com.application.messagechat.activity

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.application.messagechat.R
import com.application.messagechat.databinding.ActivityUpdateUserBinding
import com.application.messagechat.util.ControlPermission
import com.application.messagechat.util.ConvertImage
import com.application.messagechat.util.MakeAction
import com.google.firebase.firestore.FirebaseFirestore

class UpdateUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateUserBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var selectImage: Bitmap? = null
    private val firestore= FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_user)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        activityResultLauncher = MakeAction.registerActivityForSelectedImage(this){ bitmap, uri ->
            selectImage = bitmap
            binding.updateUserProfile.setImageBitmap(bitmap)
        }
        val name = intent.getStringExtra("name")
        val image = intent.getStringExtra("image")

        binding.editTextUpdateUserName.setText(name)
        binding.updateUserProfile.setImageBitmap(ConvertImage.convertToBitmap(image))

        binding.updateUserProfile.setOnClickListener {
            if (ControlPermission.galleryPermission(this)){
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            }else{
                if (Build.VERSION.SDK_INT>=33){
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),1)
                }else{
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
                }
            }
        }

        binding.buttonUpdateUser.setOnClickListener {
            updateUser(selectImage)
            setResult(RESULT_OK)
            finish()
        }

        binding.updateUserToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    fun updateUser(image: Bitmap?){
        sharedPreferences = this.getSharedPreferences("MyLoginInfo",MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId",null)
        val userName = binding.editTextUpdateUserName.text.toString()
        val updateUser = mutableMapOf<String, Any?>("userName" to userName)
        val chatRef = firestore.collection("users")
            .document(userId!!)
        if (image!=null){
            val imageToString = ConvertImage.convertToString(image)
            updateUser["profileImage"] = imageToString
            updateProfile(userId,image)
        }
        chatRef.update(updateUser)

    }

    fun updateProfile(contactId: String,profile: Bitmap){
        val profileAsString = ConvertImage.convertToString(profile)
        firestore.collection("users")
            .get().addOnSuccessListener { result->
                for (doc in result){
                    val id = doc.id
                    val contactRef = firestore.collection("users")
                        .document(id)
                        .collection("contacts")

                        contactRef.whereEqualTo("phoneNumber",contactId)
                        .get().addOnSuccessListener { documentSnapshot ->
                            if (!documentSnapshot.isEmpty){
                                for (contactDoc in documentSnapshot.documents){
                                    contactRef.document(contactDoc.id).update("contactProfile",profileAsString)
                                }
                            }
                        }
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val activity = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(activity)
            }else{
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Enable storage permission in Settings", Toast.LENGTH_LONG).show()
                }
            }

        }
    }
}