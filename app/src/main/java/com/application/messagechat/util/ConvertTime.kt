package com.application.messagechat.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConvertTime {
    companion object{
        fun convertLongToTime(timeInMillis: Long): String {
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return time.format(Date(timeInMillis))
        }
    }

}