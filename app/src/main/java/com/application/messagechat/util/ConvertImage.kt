package com.application.messagechat.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

class ConvertImage {

    companion object{

        fun convertToString(bitmap: Bitmap): String? {
            val maxSize = 300
            val ratio = minOf(
                maxSize.toFloat() / bitmap.width,
                maxSize.toFloat() / bitmap.height
            )
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            val stream = ByteArrayOutputStream()

            val resultCompress = resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)

            if (resultCompress) {
                val byteArray = stream.toByteArray()
                return Base64.encodeToString(byteArray, Base64.NO_WRAP)
            }
            return null
        }

        fun resizeImage(bitmap: Bitmap,coefficient: Double): String?{
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width*coefficient).toInt(),(bitmap.height*coefficient).toInt(),true)
            val newStream = ByteArrayOutputStream()
            val resultCompress = resizedBitmap.compress(Bitmap.CompressFormat.JPEG,60,newStream)
            if (resultCompress){
                val newByteArray = newStream.toByteArray()
                return Base64.encodeToString(newByteArray, Base64.DEFAULT)
            }else{
                return null
            }

        }



        fun convertToBitmap(imageAsString: String?): Bitmap? {
            return try {
                if (imageAsString.isNullOrEmpty()) {
                    null
                } else {
                    val byteArray = Base64.decode(imageAsString, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                }
            } catch (e: Exception) {
                null
            }
        }


    }


}