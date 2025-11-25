package com.application.messagechat.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.application.messagechat.R
import com.application.messagechat.activity.MyMessagesChatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.io.File

class MakeAction {
    companion object{
        private var recorder: MediaRecorder? = null
        fun registerActivityForSelectedImage(activity: ComponentActivity,
                                             selectedImage: (Bitmap?, Uri?) -> Unit): ActivityResultLauncher<Intent>{
            return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
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
        }

        fun makeCall(activity: Activity,phoneCall: String){
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$phoneCall")
                intent.flags     = Intent.FLAG_ACTIVITY_NEW_TASK
                activity.startActivity(intent)
        }

        fun openCamera(context: Context){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val file = createImageFile(context)
                val imageUri = FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",file)
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val activity = context as Activity
                activity.startActivityForResult(intent,2)
        }

        // to upload photo in new folder
        fun createImageFile(context: Context): File {
            val storageDir = context.getExternalFilesDir("MyChatImages")
            if (!storageDir!!.exists()) storageDir.mkdir()
            val timestamp = System.currentTimeMillis()
            return File(storageDir, "IMG_$timestamp.jpg")
        }

        fun recoreAudio(context: Context,micButton: CircleImageView){
            micButton.setOnTouchListener { v, event ->
                when(event.action){
                    MotionEvent.ACTION_DOWN ->{
                        startRecording(context)
                        micButton.setColorFilter(ContextCompat.getColor(context, R.color.backgroundColor))
                        true
                    }
                    MotionEvent.ACTION_UP ->{
                        stopRecording()
                        micButton.clearColorFilter()
                        true
                }
                    else->false
                }

            }
        }
        fun startRecording(context: Context){
            try {
                val outputDir = context.externalCacheDir ?: context.cacheDir
                val audioFile = File(outputDir, "recording_${System.currentTimeMillis()}.mp3")
                val audioFilePath = audioFile.absolutePath
                recorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFilePath)
                    prepare()
                    start()
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        fun stopRecording(){
            try {
                recorder?.apply {
                    stop()
                    pause()
                }
                recorder = null
            }catch (e: Exception){
                e.printStackTrace()
            }

        }

        fun attachementUpload(activity: ComponentActivity,
                              uploadDoc:(driveUrl:String?,mimeType:String)-> Unit): ActivityResultLauncher<String>{
            return activity.registerForActivityResult(ActivityResultContracts.GetContent()){ uri->
                if (uri!=null){
                    val account = GoogleSignIn.getLastSignedInAccount(activity)
                    Log.d("Drive Account","account: ${account?.email}")

                    val mimeType = activity.contentResolver.getType(uri) ?: "application stream"
                    val fileName = "File_${System.currentTimeMillis()}"
                    if (account!=null){
                        activity.lifecycleScope.launch {
                            val driveUrl = GoogleDriveUploader.uploadToDrive(activity,account,uri,mimeType,fileName)
                            uploadDoc(driveUrl,mimeType)
                        }
                    }else{
                        if (activity is MyMessagesChatActivity) activity.promptSignIn()
                        Toast.makeText(activity, "Please sign in to Google Drive first.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
}

