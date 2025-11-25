package com.application.messagechat.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.application.messagechat.R
import com.application.messagechat.activity.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessageNotification: FirebaseMessagingService() {

    private lateinit var sharedPreferences: SharedPreferences


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sharedPreferences = getSharedPreferences("MyLoginInfo",MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId",null)
        if (userId!=null){
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("deviceToken",token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let {
            showNotification(it.title?: "New Message", it.body?:"")
        }
    }

    fun showNotification(title:String,message:String){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,0,intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this)
            .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.einfochips_logo))
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= 30){
            val channel = NotificationChannel("Message Chat","Message Notification",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())
    }
}