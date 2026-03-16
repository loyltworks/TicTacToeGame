package com.loyltworks.tictactoegame

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.loyltworks.tictactoegame.databinding.ActivitySinglePlayerBinding
import kotlin.math.sqrt

class SinglePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySinglePlayerBinding
    private var gridSize = 3
    private lateinit var board: MutableList<String>
    
    private var chance = "X"
    private var gameOver = false

    private val playerSymbol = "X"
    private val computerSymbol = "O"

    private val buttons = mutableListOf<AppCompatButton>()
    private val imageViews = mutableListOf<ImageView>()

    private var currentTheme = "NIGHT"
    private var currentDifficulty = "EASY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySinglePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false

        currentTheme = intent.getStringExtra("THEME") ?: "NIGHT"
        currentDifficulty = intent.getStringExtra("DIFFICULTY") ?: "EASY"

        // Map Difficulty to Grid Size
        gridSize = when (currentDifficulty) {
            "MEDIUM" -> 6
            "HARD" -> 9
            else -> 3
        }

        setupThemeUI()

        binding.buttonReset.setOnClickListener { resetGame() }

        binding.imageViewBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        setupBoard()
    }

    private fun setupThemeUI() {
        var themeColor = resources.getColor(R.color.lightOrenge, null) // Default Night
        var textColor = android.graphics.Color.WHITE

        when (currentTheme) {
            "CHESS" -> {
                binding.main.setBackgroundResource(R.drawable.chess_bg)
                binding.imageViewBack.setColorFilter(android.graphics.Color.WHITE)
                themeColor = android.graphics.Color.BLACK
                textColor = android.graphics.Color.WHITE
            }
            "CARDS" -> {
                binding.main.setBackgroundResource(R.drawable.heart_spade_bg)
                binding.imageViewBack.clearColorFilter()
                binding.boardContainer.setBackgroundResource(R.drawable.cards_board_bg)
                themeColor = android.graphics.Color.BLACK
                textColor = android.graphics.Color.WHITE
            }
            "NEON" -> {
                binding.main.setBackgroundResource(R.drawable.neon_bg)
                binding.imageViewBack.setColorFilter(android.graphics.Color.WHITE)
                binding.boardContainer.setBackgroundResource(android.R.color.transparent)
                themeColor = android.graphics.Color.WHITE
                textColor = android.graphics.Color.WHITE
            }
            else -> {
                binding.main.setBackgroundResource(R.drawable.defalut_bg)
                binding.imageViewBack.setColorFilter(android.graphics.Color.parseColor("#1A2433"))
                binding.boardContainer.setBackgroundResource(R.drawable.board_bg)
                themeColor = android.graphics.Color.parseColor("#1A2433")
                textColor = android.graphics.Color.parseColor("#1A2433")
            }
        }

        // Standardize Reset Button Styling: Default theme (Night/else) -> Black, others -> White
        val resetBtnColor = if (currentTheme == "CHESS" || currentTheme == "CARDS" || currentTheme == "NEON") {
            android.graphics.Color.WHITE
        } else {
            android.graphics.Color.BLACK
        }

        binding.buttonReset.setTextColor(resetBtnColor)
        (binding.buttonReset.background as? android.graphics.drawable.GradientDrawable)?.setStroke(
            (1 * resources.displayMetrics.density).toInt(),
            resetBtnColor
        )

        binding.tvTitle.setTextColor(textColor)
    }

    private fun resetGame() {
        gameOver = false
        chance = "X"
        binding.winLine.visibility = View.INVISIBLE
        
        // Clear Glide targets to prevent glitches on reset
        for (iv in imageViews) {
            Glide.with(this).clear(iv)
        }
        
        binding.linesContainer.removeAllViews()
        setupBoard()
    }

    private fun setupBoard() {
        val size = gridSize
        board = MutableList(size * size) { "" }
        buttons.clear()
        imageViews.clear()
        binding.gridLayout.removeAllViews()
        binding.gridLayout.columnCount = size
        binding.gridLayout.rowCount = size

        val density = resources.displayMetrics.density
        
        // Dynamic board sizing based on grid size
        val totalWidthDp = when (size) {
            3 -> 300
            6 -> 330
            else -> 360 // 9x9
        }
        
        val paddingDp = if (currentTheme == "NIGHT" || currentTheme == "CARDS") 5 else 0
        
        val totalPx = (totalWidthDp * density).toInt()
        val basePaddingPx = (paddingDp * density).toInt()
        val availablePx = totalPx - 2 * basePaddingPx
        
        val cellSizePx = (availablePx / size)
        val extraPx = availablePx - (cellSizePx * size)
        val finalPaddingPx = basePaddingPx + extraPx / 2
        
        binding.boardContainer.setPadding(finalPaddingPx, finalPaddingPx, finalPaddingPx, finalPaddingPx)

        // Dynamic margin and corner radius
        val marginDp = when (size) {
            3 -> 4
            6 -> 3
            else -> 2 // 9x9
        }
        val marginPx = (marginDp * density).toInt()
        
        val cornerRadiusDp = when (size) {
            3 -> 13f
            6 -> 11f
            else -> 8f // 9x9
        }
        val cornerRadiusPx = cornerRadiusDp * density

        for (i in 0 until size * size) {
            val cellFrame = FrameLayout(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSizePx - 2 * marginPx
                    height = cellSizePx - 2 * marginPx
                    setMargins(marginPx, marginPx, marginPx, marginPx)
                }
            }

            val button = AppCompatButton(this).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                
                // Create programmatic background to control corner radius
                val shape = android.graphics.drawable.GradientDrawable()
                shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                shape.cornerRadius = cornerRadiusPx
                
                // Set color based on theme
                val bgColor = when (currentTheme) {
                    "CHESS" -> if ((i / size + i % size) % 2 == 0) android.graphics.Color.WHITE else android.graphics.Color.LTGRAY
                    "CARDS" -> android.graphics.Color.parseColor("#838380")
                    "NEON" -> android.graphics.Color.TRANSPARENT
                    else -> android.graphics.Color.parseColor("#5D543D") // Default NIGHT
                }
                shape.setColor(bgColor)
                
                if (currentTheme == "NEON") {
                    shape.setStroke((1 * density).toInt(), android.graphics.Color.WHITE)
                } else if (currentTheme == "CHESS" || currentTheme == "CARDS") {
                    shape.setStroke((1 * density).toInt(), android.graphics.Color.BLACK)
                }
                
                background = shape
                
                setOnClickListener {
                    if (!gameOver && board[i].isEmpty() && chance == playerSymbol) {
                        playMove(i)
                    }
                }
            }
            buttons.add(button)
            cellFrame.addView(button)

            // Set ImageView to MATCH_PARENT to fill the box
            val imageView = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = Gravity.CENTER
                }
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                scaleType = ImageView.ScaleType.FIT_CENTER
                visibility = View.GONE
            }
            imageViews.add(imageView)
            cellFrame.addView(imageView)

            binding.gridLayout.addView(cellFrame)
        }
    }

    private fun getThemeLineColor(): Int {
        return when (currentTheme) {
            "NEON" -> android.graphics.Color.parseColor("#77FAE3")
            "CARDS" -> android.graphics.Color.WHITE
            "CHESS" -> android.graphics.Color.WHITE
            else -> android.graphics.Color.parseColor("#FBF0B3") // Default NIGHT
        }
    }

    private fun playMove(index: Int) {
        board[index] = chance

        // Check for win to decide sound
        if (checkWinGeneric(board, chance)) {
            SoundHelper.playSound(this, R.raw.win_line)
        } else {
            SoundHelper.playSound(this, R.raw.flip)
        }

        val imageView = imageViews[index]
        val button = buttons[index]

        button.isEnabled = false
        imageView.visibility = View.VISIBLE

        when (currentTheme) {
            "CHESS" -> {
                // Remove setImageResource to avoid blinking with loadGif
                loadGif(if (chance == "X") R.drawable.king_gif else R.drawable.queen_gif, imageView)
            }
            "CARDS" -> {
                loadGif(if (chance == "X") R.drawable.spade_gif else R.drawable.heart_gif, imageView)
            }
            "NEON" -> {
                loadGif(if (chance == "X") R.drawable.neon_x_gif else R.drawable.neon_o_gif, imageView)
            }
            else -> {
                loadGif(if (chance == "X") R.drawable.x_gif else R.drawable.o_gif, imageView)
            }
        }

        if (checkWin(board, chance)) { // Check if CURRENT player won
            gameOver = true
            return
        }

        if (board.all { it.isNotEmpty() }) {
            gameOver = true
            showTieDialog()
            return
        }

        chance = if (chance == "X") "O" else "X"

        if (!gameOver && chance == computerSymbol) {
             Handler(Looper.getMainLooper()).postDelayed({
                computerMove()
            }, 600)
        }
    }

    private fun loadGif(resourceId: Int, imageView: ImageView) {
        Glide.with(this)
            .asGif()
            .load(resourceId)
            .placeholder(android.R.color.transparent)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>, isFirstResource: Boolean): Boolean = false
                override fun onResourceReady(resource: GifDrawable, model: Any, target: Target<GifDrawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    resource.setLoopCount(1)
                    resource.setVisible(true, true)
                    resource.stop()
                    resource.start()
                    return false
                }
            })
            .into(imageView)
    }


    // ==========================
    // COMPUTER AI (Generic)
    // ==========================

    private fun computerMove() {
        val position = getBestMove()
        if (position != -1) {
            playMove(position)
        }
    }

    private fun getBestMove(): Int {
        val size = gridSize
        val winLen = getWinLength()

        // 1. WIN: Check if computer can win in one move
        for (i in board.indices) {
            if (board[i].isEmpty()) {
                board[i] = computerSymbol
                if (checkWinGeneric(board, computerSymbol)) {
                    board[i] = "" // backtrack
                    return i
                }
                board[i] = "" // backtrack
            }
        }

        // 2. BLOCK: Check if player can win in one move
        for (i in board.indices) {
            if (board[i].isEmpty()) {
                board[i] = playerSymbol
                if (checkWinGeneric(board, playerSymbol)) {
                    board[i] = "" // backtrack
                    return i
                }
                board[i] = "" // backtrack
            }
        }

        // 3. ADVANCED HEURISTIC (For Medium and Hard)
        if (currentDifficulty != "EASY") {
            return getHeuristicMove()
        }

        // 4. Random (Easy / Fallback)
        val available = board.indices.filter { board[it].isEmpty() }
        if (available.isNotEmpty()) return available.random()

        return -1
    }

    private fun getHeuristicMove(): Int {
        val size = gridSize
        val scores = IntArray(size * size) { 0 }

        for (i in board.indices) {
            if (board[i].isEmpty()) {
                scores[i] = evaluateCell(i)
            } else {
                scores[i] = -1 // Taken
            }
        }

        var maxScore = -2
        val bestMoves = mutableListOf<Int>()

        for (i in scores.indices) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                bestMoves.clear()
                bestMoves.add(i)
            } else if (scores[i] == maxScore) {
                bestMoves.add(i)
            }
        }

        return if (bestMoves.isNotEmpty()) bestMoves.random() else -1
    }

    private fun evaluateCell(index: Int): Int {
        val size = gridSize
        val winLen = getWinLength()
        val r = index / size
        val c = index % size
        var score = 0

        val directions = listOf(
            Pair(0, 1),   // Horizontal
            Pair(1, 0),   // Vertical
            Pair(1, 1),   // Diagonal SE
            Pair(1, -1)   // Diagonal SW
        )

        for ((dr, dc) in directions) {
            // Check all windows of length winLen that could contain this cell
            for (offset in 0 until winLen) {
                val startR = r - offset * dr
                val startC = c - offset * dc
                val endR = startR + (winLen - 1) * dr
                val endC = startC + (winLen - 1) * dc

                // Check window bounds
                if (startR in 0 until size && startC in 0 until size &&
                    endR in 0 until size && endC in 0 until size) {
                    
                    var compCount = 0
                    var playerCount = 0

                    for (k in 0 until winLen) {
                        val currR = startR + k * dr
                        val currC = startC + k * dc
                        val symbol = board[currR * size + currC]
                        if (symbol == computerSymbol) compCount++
                        else if (symbol == playerSymbol) playerCount++
                    }

                    score += calculateWindowScore(compCount, playerCount)
                }
            }
        }

        // Bias towards center to start strong or block central growth
        val center = size / 2
        val dist = Math.abs(r - center) + Math.abs(c - center)
        score += (size * 2 - dist) // Small bonus for proximity to center

        return score
    }

    private fun calculateWindowScore(compCount: Int, playerCount: Int): Int {
        // If window contains both symbols, no one can win in this line
        if (compCount > 0 && playerCount > 0) return 0

        // Offensive score (Potential for computer to win)
        if (compCount > 0) {
            return when (compCount) {
                4 -> 10000 // Almost win
                3 -> 1000
                2 -> 100
                1 -> 10
                else -> 0
            }
        }

        // Defensive score (Potential for player to win - block it)
        if (playerCount > 0) {
            return when (playerCount) {
                4 -> 5000 // Strongly block player win
                3 -> 500
                2 -> 50
                1 -> 5
                else -> 0
            }
        }

        return 1 // Empty but playable line
    }
    
    // Win Helper that doesn't trigger UI effects, just logic
     private fun checkWinGeneric(bd: List<String>, s: String): Boolean {
         val winLen = getWinLength()
         val size = gridSize
         
        for (i in 0 until size * size) {
            val row = i / size
            val col = i % size
            
            // Horizontal
            if (col + winLen <= size) {
                if ((0 until winLen).all { k -> bd[i + k] == s }) return true
            }
            // Vertical
            if (row + winLen <= size) {
                if ((0 until winLen).all { k -> bd[i + k * size] == s }) return true
            }
            // Diagonal SE
            if (col + winLen <= size && row + winLen <= size) {
                if ((0 until winLen).all { k -> bd[i + k * (size + 1)] == s }) return true
            }
             // Diagonal SW
            if (col - winLen + 1 >= 0 && row + winLen <= size) {
                if ((0 until winLen).all { k -> bd[i + k * (size - 1)] == s }) return true
            }
        }
        return false
     }

    // ==========================
    // WIN LOGIC & EFFECT
    // ==========================

    private fun getWinLength(): Int {
        return when (gridSize) {
            3 -> 3
            6 -> 4 // Usually 4 or 5 for 6x6? Let's stick to 3 for simplicity or 4 if standard. TicTacToe 6x6 usually needs 4.
            9 -> 5 // TicTacToe 9x9 usually needs 5.
            else -> 3
        }
    }

    private fun checkWin(bd: List<String>, s: String): Boolean {
         val winLen = getWinLength()
         val size = gridSize
         
        for (i in 0 until size * size) {
            val row = i / size
            val col = i % size
            
            if (bd[i] != s) continue

            // Horizontal
            if (col + winLen <= size) {
                if (checkSequence(i, 1, winLen)) return true
            }
            // Vertical
            if (row + winLen <= size) {
                if (checkSequence(i, size, winLen)) return true
            }
            // Diagonal SE
            if (col + winLen <= size && row + winLen <= size) {
                if (checkSequence(i, size + 1, winLen)) return true
            }
             // Diagonal SW
            if (col - winLen + 1 >= 0 && row + winLen <= size) {
                if (checkSequence(i, size - 1, winLen)) return true
            }
        }
        return false
    }

    private fun checkSequence(start: Int, step: Int, length: Int): Boolean {
        val p = board[start]
        for (k in 1 until length) {
            if (board[start + k * step] != p) return false
        }
        winEffect(start, step, length)
        return true
    }

    private fun winEffect(start: Int, step: Int, length: Int) {
        // VIbration
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(200)
            }
        }

        // Animation
        binding.boardContainer.animate()
            .rotation(3f)
            .setDuration(100)
            .withEndAction {
                binding.boardContainer.animate()
                    .rotation(0f)
                    .setDuration(100)
                    .start()
            }.start()

        // WIN LINE
        val line = binding.winLine
        line.visibility = View.VISIBLE
        line.setBackgroundResource(R.drawable.white_line)
        line.background.setTint(getThemeLineColor())

        val density = resources.displayMetrics.density
        val totalWidthDp = when (gridSize) {
            3 -> 300
            6 -> 330
            else -> 360
        }
        val paddingDp = if (currentTheme == "NIGHT" || currentTheme == "CARDS") 5 else 0
        val totalPx = (totalWidthDp * density).toInt()
        val basePaddingPx = (paddingDp * density).toInt()
        val availablePx = totalPx - 2 * basePaddingPx
        val cellSizePx = (availablePx / gridSize)

        val startRow = start / gridSize
        val startCol = start % gridSize

        val endIndex = start + (length - 1) * step
        val endRow = endIndex / gridSize
        val endCol = endIndex % gridSize

        val startX = startCol * cellSizePx + cellSizePx / 2f
        val startY = startRow * cellSizePx + cellSizePx / 2f
        val endX = endCol * cellSizePx + cellSizePx / 2f
        val endY = endRow * cellSizePx + cellSizePx / 2f

        val deltaX = endX - startX
        val deltaY = endY - startY
        val lineLen = sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()
        val angle = Math.toDegrees(Math.atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat()

        line.layoutParams.width = lineLen.toInt()
        line.requestLayout()

        line.pivotX = 0f
        line.pivotY = (6 * density) / 2f
        line.translationX = startX
        line.translationY = startY - (6 * density) / 2f
        line.rotation = angle

        line.scaleX = 0f
        line.animate().scaleX(1f).setDuration(400).start()

        val winnerText = if (chance == "X") "X" else "Computer"
        
        Handler(Looper.getMainLooper()).postDelayed({
            showConfirmationDialog(winnerText)
        }, 1500)
    }

    private fun showTieDialog() {
        Handler(Looper.getMainLooper()).postDelayed({
            showConfirmationDialog("TIE")
        }, 1000)
    }

    private fun showConfirmationDialog(winner: String) {
        if (winner == "TIE") {
            SoundHelper.playSound(this, R.raw.tie)
        } else {
            SoundHelper.playSound(this, R.raw.winning)
        }
        try {
            ConfirmationDialog.showDialog(this, winner, currentTheme, object : ConfirmationDialog.ConfirmationCallback {
                override fun onResetBtn() {
                    resetGame()
                }
            })
        } catch (e: Exception) {
             AlertDialog.Builder(this)
                .setTitle(if (winner == "TIE") "It's a Tie!" else "$winner Wins!")
                .setPositiveButton("Play Again") { _, _ -> resetGame() }
                .setCancelable(false)
                .show()
        }
    }
}
