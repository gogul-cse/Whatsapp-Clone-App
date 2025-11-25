package com.application.messagechat.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.application.messagechat.R
import com.application.messagechat.databinding.ActivityViewStatusBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_status)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val id  = intent.getIntExtra("id",-1)


        binding.materialToolbar3.setNavigationOnClickListener {
            finish()
        }
    }

    fun converLongToTime(timeMilles: Long): String{
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return time.format(Date(timeMilles))
    }
}