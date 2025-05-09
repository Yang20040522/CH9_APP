package com.example.ch9

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random // ✅ 改為實際使用 Random 套件

// 🧠 定義選手資料類別
data class Runner(
    val name: String,
    val baseSpeed: Long,        // 每一步的基本延遲（越小越快）
    val burstChance: Double,    // 發動爆發的機率
    val burstSpeed: Long        // 爆發時的速度（延遲時間）
)

class MainActivity : AppCompatActivity() {

    private lateinit var progressBars: List<ProgressBar>
    private lateinit var guessGroup: RadioGroup
    private lateinit var startButton: Button
    private lateinit var scoreText: TextView
    private var userScore = 0
    private var isFinished = false

    // 🏃‍♂️ 各選手屬性（盡可能平衡設計）
    private val runners = listOf(
        Runner("選手 1", baseSpeed = 50, burstChance = 0.0, burstSpeed = 50),   // 穩定型，平均最快
        Runner("選手 2", baseSpeed = 67, burstChance = 0.35, burstSpeed = 12), // 爆發型
        Runner("選手 3", baseSpeed = 73, burstChance = 0.28, burstSpeed = 8)   // 不穩定型，偶爾超快
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 對應 UI 元件
        progressBars = listOf(
            findViewById(R.id.progress1),
            findViewById(R.id.progress2),
            findViewById(R.id.progress3)
        )
        guessGroup = findViewById(R.id.guessGroup)
        scoreText = findViewById(R.id.scoreText)
        startButton = findViewById(R.id.startButton)

        // 初始分數顯示
        scoreText.text = getString(R.string.score_label, 0)

        startButton.setOnClickListener {
            Toast.makeText(this, "🏃 選手屬性已套用：穩定 / 爆發 / 波動", Toast.LENGTH_SHORT).show()
            startRace()
        }
    }

    private fun startRace() {
        // 重置
        for (bar in progressBars) {
            bar.progress = 0
        }
        isFinished = false

        // 每位選手一個執行緒
        val threads = mutableListOf<Thread>()
        for (i in progressBars.indices) {
            val runner = runners[i]
            val thread = Thread {
                for (j in 1..100) {
                    // ✅ 隨機是否爆發
                    val delay = if (Random.nextDouble() < runner.burstChance)
                        runner.burstSpeed
                    else
                        runner.baseSpeed

                    Thread.sleep(delay)

                    runOnUiThread {
                        progressBars[i].progress = j
                        if (j == 100 && !isFinished) {
                            isFinished = true
                            handleResult(i)
                        }
                    }

                    if (isFinished) break
                }
            }
            threads.add(thread)
        }

        threads.forEach { it.start() }
    }

    private fun handleResult(winnerIndex: Int) {
        val guessId = guessGroup.checkedRadioButtonId
        val guess = when (guessId) {
            R.id.guess1 -> 0
            R.id.guess2 -> 1
            R.id.guess3 -> 2
            else -> -1
        }

        val winnerName = getString(
            when (winnerIndex) {
                0 -> R.string.runner1
                1 -> R.string.runner2
                2 -> R.string.runner3
                else -> R.string.app_name
            }
        )

        val resultText = if (guess == winnerIndex) {
            userScore++
            getString(R.string.win_correct, winnerName)
        } else {
            getString(R.string.win_wrong, winnerName)
        }

        Toast.makeText(this, resultText, Toast.LENGTH_SHORT).show()
        scoreText.text = getString(R.string.score_label, userScore)
    }
}
