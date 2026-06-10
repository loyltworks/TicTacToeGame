package com.loyltworks.tictactoegame

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.loyltworks.tictactoegame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private var selectedTheme = "NIGHT"
    private var selectedDifficulty = "EASY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false

        Glide.with(this)
            .asGif()
            .load(R.drawable.logo_gif)
            .into(binding.ivLogo)
        
        loadPreferences()

        val animZoom2 = AnimationUtils.loadAnimation(applicationContext, R.anim.zoom2)

        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        binding.btnSingle.setOnClickListener{
            binding.btnSingle.startAnimation(animZoom2)
            val intent = Intent(this@MainActivity, SinglePlayerActivity::class.java)
            intent.putExtra("THEME", selectedTheme)
            intent.putExtra("DIFFICULTY", selectedDifficulty)
            startActivity(intent)
        }

        binding.btnMulti.setOnClickListener{
            binding.btnMulti.startAnimation(animZoom2)
            val intent = Intent(this@MainActivity, MultiplayerActivity::class.java)
            intent.putExtra("THEME", selectedTheme)
            intent.putExtra("DIFFICULTY", selectedDifficulty)
            startActivity(intent)
        }
        onBackPressedDispatcher.addCallback(this) {
            finishAffinity()
        }
    }

    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        selectedTheme = sharedPreferences.getString("THEME", "NIGHT") ?: "NIGHT"
        selectedDifficulty = sharedPreferences.getString("DIFFICULTY", "EASY") ?: "EASY"
    }

    private fun savePreferences(theme: String, difficulty: String) {
        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("THEME", theme)
        editor.putString("DIFFICULTY", difficulty)
        editor.apply()
        
        selectedTheme = theme
        selectedDifficulty = difficulty
    }
     override fun onResume() {
        super.onResume()
        loadPreferences() // Reload incase changed
    }

    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Theme Views
        val llNight = dialogView.findViewById<android.widget.LinearLayout>(R.id.llNight)
        val llCards = dialogView.findViewById<android.widget.LinearLayout>(R.id.llCards)
        val llNeon = dialogView.findViewById<android.widget.LinearLayout>(R.id.llNeon)
        val llChess = dialogView.findViewById<android.widget.LinearLayout>(R.id.llChess)
        val tvNight = dialogView.findViewById<android.widget.TextView>(R.id.tvNight)
        val tvCards = dialogView.findViewById<android.widget.TextView>(R.id.tvCards)
        val tvNeon = dialogView.findViewById<android.widget.TextView>(R.id.tvNeon)
        val tvChess = dialogView.findViewById<android.widget.TextView>(R.id.tvChess)

        // Level Views
        val tvEasy = dialogView.findViewById<android.widget.TextView>(R.id.tvEasy)
        val tvMedium = dialogView.findViewById<android.widget.TextView>(R.id.tvMedium)
        val tvHard = dialogView.findViewById<android.widget.TextView>(R.id.tvHard)

        // Theme GIFs
        val ivNightTheme = dialogView.findViewById<android.widget.ImageView>(R.id.ivNightTheme)
        val ivCardsTheme = dialogView.findViewById<android.widget.ImageView>(R.id.ivCardsTheme)
        val ivNeonTheme = dialogView.findViewById<android.widget.ImageView>(R.id.ivNeonTheme)
        val ivChessTheme = dialogView.findViewById<android.widget.ImageView>(R.id.ivChessTheme)

        Glide.with(this).asGif().load(R.drawable.xo_theme_gif).into(ivNightTheme)
        Glide.with(this).asGif().load(R.drawable.heart_spade_theme_gif).into(ivCardsTheme)
        Glide.with(this).asGif().load(R.drawable.neon_theme_gif).into(ivNeonTheme)
        Glide.with(this).asGif().load(R.drawable.king_queen_theme_gif).into(ivChessTheme)

        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)

        // Temporary state holders
        var currentDialogTheme = selectedTheme
        var currentDialogDifficulty = selectedDifficulty

        // Helper to update Theme UI
        fun updateThemeUI(theme: String) {
            currentDialogTheme = theme
            
            // Reset all
            val unselectedBg = R.drawable.theme_unselected_bg
            val selectedBg = R.drawable.theme_selected_bg
            val colorBlack = resources.getColor(R.color.black, null)
            val colorWhite = resources.getColor(R.color.white, null)

            llNight.setBackgroundResource(if (theme == "NIGHT") selectedBg else unselectedBg)
            tvNight.setTextColor(if (theme == "NIGHT") colorWhite else colorBlack)

            llCards.setBackgroundResource(if (theme == "CARDS") selectedBg else unselectedBg)
            tvCards.setTextColor(if (theme == "CARDS") colorWhite else colorBlack)

            llNeon.setBackgroundResource(if (theme == "NEON") selectedBg else unselectedBg)
            tvNeon.setTextColor(if (theme == "NEON") colorWhite else colorBlack)

            llChess.setBackgroundResource(if (theme == "CHESS") selectedBg else unselectedBg)
            tvChess.setTextColor(if (theme == "CHESS") colorWhite else colorBlack)
        }

        // Helper to update Level UI
        fun updateLevelUI(level: String) {
            currentDialogDifficulty = level
            
            val selectedBg = R.drawable.level_selected_bg
            val colorBrown = resources.getColor(R.color.brown, null)
            val colorWhite = resources.getColor(R.color.white, null)

            tvEasy.setBackgroundResource(if (level == "EASY") selectedBg else 0)
            tvEasy.setTextColor(if (level == "EASY") colorWhite else colorBrown)

            tvMedium.setBackgroundResource(if (level == "MEDIUM") selectedBg else 0)
            tvMedium.setTextColor(if (level == "MEDIUM") colorWhite else colorBrown)

            tvHard.setBackgroundResource(if (level == "HARD") selectedBg else 0)
            tvHard.setTextColor(if (level == "HARD") colorWhite else colorBrown)
        }

        // Initialize UI
        updateThemeUI(selectedTheme)
        updateLevelUI(selectedDifficulty)

        // Listeners
        llNight.setOnClickListener { updateThemeUI("NIGHT") }
        llCards.setOnClickListener { updateThemeUI("CARDS") }
        llNeon.setOnClickListener { updateThemeUI("NEON") }
        llChess.setOnClickListener { updateThemeUI("CHESS") }

        tvEasy.setOnClickListener { updateLevelUI("EASY") }
        tvMedium.setOnClickListener { updateLevelUI("MEDIUM") }
        tvHard.setOnClickListener { updateLevelUI("HARD") }

        btnSave.setOnClickListener {
            savePreferences(currentDialogTheme, currentDialogDifficulty)
            dialog.dismiss()
        }

        dialog.show()
    }
}
