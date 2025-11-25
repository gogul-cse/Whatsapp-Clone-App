package com.application.messagechat.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class ControlPermission {
    companion object{
        fun galleryPermission(context: Context): Boolean{
            return if (Build.VERSION.SDK_INT>=33){
                ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_MEDIA_IMAGES)== PackageManager.PERMISSION_GRANTED
            }else{
                ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
            }
        }

        fun callPermission(context: Context): Boolean{
            return  (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED)

        }

        fun cameraPermission(context: Context): Boolean{
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
        }

        fun micPermission(context: Context): Boolean{
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED)
        }

    }
}