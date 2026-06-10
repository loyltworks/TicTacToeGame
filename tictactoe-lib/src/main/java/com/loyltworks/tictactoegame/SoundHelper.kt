package com.loyltworks.tictactoegame

import android.content.Context
import android.media.MediaPlayer

object SoundHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(context: Context, soundResId: Int) {
        try {
            // Stop and release previous player if any
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            
            // Create and start new player
            mediaPlayer = MediaPlayer.create(context, soundResId)
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                if (mediaPlayer == mp) {
                    mediaPlayer = null
                }
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
