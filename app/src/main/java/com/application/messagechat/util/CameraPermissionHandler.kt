package com.application.messagechat.util

import android.Manifest
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class CameraPermissionHandler(private val activity: AppCompatActivity) {

    val cameraLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
            if (isGranted){
                MakeAction.openCamera(activity)
            }else{
                Toast.makeText(activity, "Enable Camera permission in Settings", Toast.LENGTH_LONG).show()
            }
        }
    fun requestCameraPermission(){
        if (ControlPermission.cameraPermission(activity)){
            MakeAction.openCamera(activity)
        }else{
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}