package com.papy.launcher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow

object MissedCalls {
    val count = MutableStateFlow(0)

    fun refresh(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED
        ) {
            count.value = 0
            return
        }
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE),
                "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.NEW} = ?",
                arrayOf(CallLog.Calls.MISSED_TYPE.toString(), "1"),
                null
            )
            count.value = cursor?.use { c -> c.count } ?: 0
        } catch (e: Exception) {
            count.value = 0
        }
    }
}