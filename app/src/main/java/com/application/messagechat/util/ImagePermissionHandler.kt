package com.application.messagechat.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ImagePermissionHandler(val activity: AppCompatActivity,val selectedImage:(Bitmap?, Uri?) -> Unit) {

    private val galleryPermission = if (Build.VERSION.SDK_INT >= 33){
        Manifest.permission.READ_MEDIA_IMAGES
    }else{
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val imagePickerLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if (result.resultCode == Activity.RESULT_OK && result.data != null){
                val imageUri = result.data!!.data

                val bitmap = imageUri?.let{
                    if (Build.VERSION.SDK_INT>=33){
                        val imageSource = ImageDecoder.createSource(activity.contentResolver,it)
                        ImageDecoder.decodeBitmap(imageSource)
                    }else{
                        MediaStore.Images.Media.getBitmap(activity.contentResolver,it)
                    }
                }
                selectedImage(bitmap,imageUri)
            }else{
                selectedImage(null,null)
            }
        }
    val imageLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
            if (isGranted){
                openGallery()
            }else{
                Toast.makeText(activity, "Enable Gallery permission in Settings", Toast.LENGTH_LONG).show()
            }
        }

    fun requestGalleryPermission(){
        if (ControlPermission.galleryPermission(activity)){
            openGallery()
        }else{
            imageLauncher.launch(galleryPermission)
        }
    }

    fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
}