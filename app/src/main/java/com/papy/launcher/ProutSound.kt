package com.papy.launcher

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlin.concurrent.thread

fun playFartSound(context: Context) {
    thread {
        try {
            val mp = MediaPlayer.create(context, R.raw.prout)
            if (mp == null) {
                Log.e("ProutSound", "MediaPlayer.create a retourné null — res/raw/prout.mp3 introuvable ou illisible")
                return@thread
            }
            mp.setOnCompletionListener { it.release() }
            mp.setVolume(1.0f, 1.0f)
            mp.start()
        } catch (e: Exception) {
            Log.e("ProutSound", "Erreur lecture prout.mp3", e)
        }
    }
}