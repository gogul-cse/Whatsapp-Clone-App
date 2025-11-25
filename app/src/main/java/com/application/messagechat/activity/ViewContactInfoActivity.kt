package com.application.messagechat.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.application.messagechat.R
import com.application.messagechat.databinding.ActivityViewContactInfoBinding
import com.application.messagechat.util.CallPermissionHandler
import com.application.messagechat.util.CameraPermissionHandler
import com.application.messagechat.util.ControlPermission
import com.application.messagechat.util.ConvertImage
import com.application.messagechat.util.MakeAction
import com.google.firebase.firestore.FirebaseFirestore

class ViewContactInfoActivity : AppCompatActivity() {

    lateinit var binding: ActivityViewContactInfoBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var phoneNumber: String? = null
    private var contactName: String? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var callLauncher: CallPermissionHandler
    private lateinit var cameraLauncher: CameraPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_contact_info)
        callLauncher = CallPermissionHandler(this)
        cameraLauncher = CameraPermissionHandler(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == RESULT_OK){
                refreshPage()
            }
        }
        val profileImage = intent.getStringExtra("image")
        binding.viewContactProfileImage.setImageBitmap(ConvertImage.convertToBitmap(profileImage))

        binding.viewContactToolBar.setNavigationOnClickListener {
            finish()
        }
        binding.imageViewCall.setOnClickListener {
            callLauncher.setphoneNumber(phoneNumber!!)
            callLauncher.requestCallPermission()
        }
        binding.imageViewTakePhoto.setOnClickListener {
            cameraLauncher.requestCameraPermission()
        }

        binding.viewContactToolBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.editContact ->{
                    val intent = Intent(this, UpdateContactActivity::class.java)
                    intent.putExtra("name",contactName)
                    intent.putExtra("phoneNumber",phoneNumber)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        binding.imagemessage.setOnClickListener { finish() }

    }
    fun refreshPage(){
        val contactId = intent.getStringExtra("contactId")
        val userId = intent.getStringExtra("userId")

        firestore.collection("users")
            .document(userId!!)
            .collection("contacts")
            .whereEqualTo("id",contactId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty && result.documents.isNotEmpty()){
                    val document = result.documents[0]
                    phoneNumber = document.getString("phoneNumber")
                    contactName = document.getString("name")
                    binding.textViewPersonName.text = contactName
                    binding.textViewPersonPhoneNumber.text = phoneNumber

                }

            }
    }

    override fun onResume() {
        super.onResume()
        refreshPage()
    }

}