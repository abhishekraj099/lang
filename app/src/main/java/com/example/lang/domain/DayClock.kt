package com.example.lang.domain

import java.util.Calendar
import java.util.concurrent.TimeUnit

object DayClock {
    fun todayEpochDay(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis)
    }
}
