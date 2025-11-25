package com.application.messagechat.activity

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.application.messagechat.R
import com.application.messagechat.databinding.ActivityAddStatusBinding
import com.application.messagechat.util.ConvertImage

class AddStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStatusBinding
    private lateinit var selectedImage: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_status)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


        binding.materialToolbar2.setNavigationOnClickListener {
            finish()
        }
        val image = intent.getStringExtra("imageAsUri")
        image?.let {
            val imageUri = Uri.parse(it)
            selectedImage = if (Build.VERSION.SDK_INT>=28){
                val imageSource = ImageDecoder.createSource(contentResolver,imageUri)
                ImageDecoder.decodeBitmap(imageSource)
            }else{
                MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
            }
            binding.imageViewAddStatus.setImageBitmap(selectedImage)
        }


        binding.sendStatus.setOnClickListener {
            val descriptionText = binding.editTextStatusDescription.text.toString()
            binding.editTextStatusDescription.setText("")

            finish()
        }
    }

}