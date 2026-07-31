package com.papy.launcher

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow

object BadgeStore {
    val sms = MutableStateFlow(0)
    val mail = MutableStateFlow(0)
    val whatsapp = MutableStateFlow(0)

    private var serviceInstance: PapyNotificationListener? = null

    fun bindService(service: PapyNotificationListener) {
        serviceInstance = service
    }

    fun unbindService() {
        serviceInstance = null
    }

    fun refreshAll() {
        serviceInstance?.countNotifications()
    }
}

class PapyNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        BadgeStore.bindService(this)
        countNotifications()
    }

    override fun onListenerDisconnected() {
        BadgeStore.unbindService()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        countNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        countNotifications()
    }

    fun countNotifications() {
        val active = try {
            activeNotifications ?: return
        } catch (e: Exception) {
            return
        }

        var smsCount = 0
        var mailCount = 0
        var waCount = 0

        for (sbn in active) {
            val pkg = sbn.packageName ?: continue
            val isGroup = sbn.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY != 0
            if (isGroup) continue
            when (pkg) {
                "com.google.android.apps.messaging",
                "com.samsung.android.app.messaging",
                "com.android.mms",
                "com.android.messaging" -> smsCount++
                "com.google.android.gm",
                "com.samsung.android.app.email",
                "com.android.email" -> mailCount++
                "com.whatsapp",
                "com.whatsapp.w4b" -> waCount++
            }
        }
        BadgeStore.sms.value = smsCount
        BadgeStore.mail.value = mailCount
        BadgeStore.whatsapp.value = waCount
    }
}