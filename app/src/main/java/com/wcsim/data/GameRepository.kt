package com.wcsim.data

import android.content.Context
import com.wcsim.engine.career.CareerState
import com.wcsim.engine.score.HighScore
import java.io.File

class GameRepository(private val context: Context) {
    private val saveFile: File get() = File(context.filesDir, "career.json")
    private val scoreFile: File get() = File(context.filesDir, "highscores.json")

    fun hasSave(): Boolean = saveFile.exists()

    fun saveCareer(state: CareerState) = saveFile.writeText(SaveCodec.encodeCareer(state))
    fun loadCareer(): CareerState? =
        if (saveFile.exists()) runCatching { SaveCodec.decodeCareer(saveFile.readText()) }.getOrNull() else null
    fun clearCareer() { if (saveFile.exists()) saveFile.delete() }

    fun loadScores(): HighScore.Table =
        if (scoreFile.exists()) runCatching { SaveCodec.decodeScores(scoreFile.readText()) }
            .getOrDefault(HighScore.Table()) else HighScore.Table()
    fun addScore(entry: HighScore.Entry) {
        val updated = HighScore.add(loadScores(), entry)
        scoreFile.writeText(SaveCodec.encodeScores(updated))
    }
}
