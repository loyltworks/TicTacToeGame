package com.loyltworks.tictactoegame

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


object ConfirmationDialog {

    private var dialog: Dialog?= null
    
    interface ConfirmationCallback{
        fun onResetBtn()
    }

    fun showDialog(context: Context, alertMsg: String, currentTheme: String, confirmationCallback: ConfirmationCallback) {
        if (dialog != null) return

        dialog = Dialog(context, R.style.Theme_Dialog)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        dialog?.setContentView(R.layout.layout_confirmation)
        dialog?.show()

        val imageBG = dialog?.findViewById<ImageView>(R.id.imageBG)
        val imageForeground = dialog?.findViewById<ImageView>(R.id.imageForeground)
        val btnBack = dialog?.findViewById<AppCompatButton>(R.id.btnBack)
        val playAgainBtn = dialog?.findViewById<AppCompatButton>(R.id.playAgainBtn)

        if (alertMsg == "X") {
            loadGif(context, imageBG, R.drawable.win_round_gif)
            val iconRes = when (currentTheme) {
                "CARDS" -> R.drawable.spade_win_gif
                "CHESS" -> R.drawable.king_win_gif
                "NEON" -> R.drawable.neon_x_win_gif
                else -> R.drawable.x_win_gif
            }
            loadGif(context, imageForeground, iconRes)
        } else if (alertMsg == "O" || alertMsg == "Computer") {

            loadGif(context, imageBG, R.drawable.win_round_gif)
            val iconRes = when (currentTheme) {
                "CARDS" -> R.drawable.heart_win_gif
                "CHESS" -> R.drawable.queen_win_gif
                "NEON" -> R.drawable.neon_o_win_gif
                else -> R.drawable.o_win_gif
            }
            loadGif(context, imageForeground, iconRes)
        } else {
            loadGif(context, imageBG, R.drawable.draw_round_gif)
            loadGif(context, imageForeground, R.drawable.draw_gif) // Handshake GIF
        }

        btnBack?.setOnClickListener {
            dialog!!.dismiss()
            dialog = null
            if (context is androidx.appcompat.app.AppCompatActivity) {
                context.finish() // Go back to MainActivity
            }
        }

        playAgainBtn?.setOnClickListener {
            confirmationCallback.onResetBtn()
            dialog!!.dismiss()
            dialog = null
        }
    }

    private fun loadGif(context: Context, imageView: ImageView?, resourceId: Int) {
        if (imageView == null) return
        Glide.with(context).clear(imageView)
        Glide.with(context)
            .asGif()
            .load(resourceId)
            .placeholder(android.R.color.transparent)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>, isFirstResource: Boolean): Boolean = false
                override fun onResourceReady(resource: GifDrawable, model: Any, target: Target<GifDrawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    // GIFs will loop continuously as requested
                    return false
                }
            })
            .into(imageView)
    }
}