package com.application.messagechat.util

import android.Manifest
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class CallPermissionHandler(val activity: AppCompatActivity) {

    private var phoneNumber: String? = null
     val callLauncher : ActivityResultLauncher<String> =
         activity.registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
            if (isGranted){
                phoneNumber?.let {
                    MakeAction.makeCall(activity,it)
                }
            }else{
                Toast.makeText(activity, "Enable Phone permission in Settings", Toast.LENGTH_LONG).show()
            }
        }
    fun setphoneNumber(number: String){
        phoneNumber = number
    }
    fun requestCallPermission(){
        if (ControlPermission.callPermission(activity)){
            phoneNumber?.let {
                MakeAction.makeCall(activity,it)
            }
        }else{
            callLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }
}