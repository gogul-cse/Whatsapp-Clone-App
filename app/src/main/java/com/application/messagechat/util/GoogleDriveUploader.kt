package com.application.messagechat.util

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections
import kotlin.io.outputStream

class GoogleDriveUploader {
    companion object{
        suspend fun uploadToDrive(context: Context, account: GoogleSignInAccount,fileUri: Uri,mimeType: String,
                          fileName: String):String? = withContext(Dispatchers.IO){
                try {
                    val credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(
                        DriveScopes.DRIVE_FILE))
                    credential.selectedAccount = account.account

                    val driveService = Drive.Builder(com.google.api.client.http.javanet.NetHttpTransport(),
                        com.google.api.client.json.gson.GsonFactory(),credential)
                        .setApplicationName("MessageChat").build()

                    val inputStream = context.contentResolver.openInputStream(fileUri)
                    val tempfile = java.io.File(context.cacheDir,fileName)
                    inputStream.use { inputStream ->
                        tempfile.outputStream().use { output->
                            inputStream?.copyTo(output)
                        }
                    }
                    val filedata = File().apply { name = fileName }
                    val mediaContent = FileContent(mimeType,tempfile)
                    val uplodedFile = driveService.files().create(filedata,mediaContent)
                        .setFields("id")
                        .execute()
                    val fileId = uplodedFile.id
                    val fileUrl = "http://drive.google.com/uc?.id=$fileId"
                    withContext(Dispatchers.Main){
                        Toast.makeText(context,"File Uploaded!", Toast.LENGTH_LONG).show()
                    }
                    fileUrl
                }catch (e: Exception){
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    null
                }
        }
    }
}