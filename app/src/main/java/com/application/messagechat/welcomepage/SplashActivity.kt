package com.application.messagechat.welcomepage

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.application.messagechat.R
import com.application.messagechat.databinding.ActivitySplashBinding
import com.application.messagechat.signup.LoginActivity
import com.application.messagechat.activity.MainActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        binding.title = "Message App"
        binding.imageSrc = ContextCompat.getDrawable(this, R.drawable.arrow)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.textViewmessageSplash.animation = AnimationUtils.loadAnimation(this, R.anim.splash_anim)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable{
            override fun run() {

                sharedPreferences = getSharedPreferences("MyLoginInfo",MODE_PRIVATE)
                val savedInfo = sharedPreferences.getBoolean("userLoged",false)
                if (savedInfo){
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }else{
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
                finish()
            }
        },2000)




    }
}